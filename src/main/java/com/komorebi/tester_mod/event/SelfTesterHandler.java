package com.komorebi.tester_mod.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.komorebi.tester_mod.ModMain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ModMain.MODID)
public final class SelfTesterHandler {

    private static final String SELF_TESTER_MODE_TAG = ModMain.MODID + ":self_tester_mode";
    private static final String DAMAGE_IMMUNITY_TAG = ModMain.MODID + ":self_tester_damage_immunity";
    private static final String OUTPUT_ZERO_DAMAGE_TAG = ModMain.MODID + ":self_tester_output_zero_damage";
    private static final Map<ServerPlayer, Deque<PendingDamage>> PENDING_HEALTH_RESTORES =
        new IdentityHashMap<>();

    private SelfTesterHandler() {
    }

    public static boolean toggle(ServerPlayer player) {
        boolean enabled = !isEnabled(player);
        player.getPersistentData().putBoolean(SELF_TESTER_MODE_TAG, enabled);
        player.displayClientMessage(Component.translatable(
            "chat.tester_mod.self_tester." + (enabled ? "enabled" : "disabled")
        ), true);
        return enabled;
    }

    public static boolean isEnabled(ServerPlayer player) {
        return player.getPersistentData().getBoolean(SELF_TESTER_MODE_TAG);
    }

    public static boolean hasDamageImmunity(ServerPlayer player) {
        return player.getPersistentData().getBoolean(DAMAGE_IMMUNITY_TAG);
    }

    public static boolean shouldOutputZeroDamage(ServerPlayer player) {
        return !player.getPersistentData().contains(OUTPUT_ZERO_DAMAGE_TAG)
            || player.getPersistentData().getBoolean(OUTPUT_ZERO_DAMAGE_TAG);
    }

    public static void applyConfiguration(ServerPlayer player, boolean damageImmunity, boolean outputZeroDamage) {
        player.getPersistentData().putBoolean(DAMAGE_IMMUNITY_TAG, damageImmunity);
        player.getPersistentData().putBoolean(OUTPUT_ZERO_DAMAGE_TAG, outputZeroDamage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
            || !isEnabled(player)) {
            return;
        }

        PendingDamage pending = new PendingDamage(
            player,
            event.getOriginalAmount(),
            event.getSource(),
            event.getSource().getMsgId(),
            player.level().getGameTime(),
            shouldOutputZeroDamage(player),
            hasDamageImmunity(player) && !event.getSource().is(DamageTypes.GENERIC_KILL)
        );

        event.addReductionModifier(DamageContainer.Reduction.ABSORPTION, (container, reduction) -> {
            float healthDamage = Math.max(0.0F, container.getNewDamage() - reduction);
            float damageAfterCooldown = Math.max(
                0.0F,
                container.getOriginalDamage() - container.getReduction(DamageContainer.Reduction.INVULNERABILITY)
            );
            float actualDamage = Math.min(player.getHealth(), healthDamage);
            if (pending.preserveHealth && actualDamage > 0.0F) {
                pending.actualDamage = actualDamage;
                PENDING_HEALTH_RESTORES
                    .computeIfAbsent(player, ignored -> new ArrayDeque<>())
                    .push(pending);
            }
            pending.complete(actualDamage, damageAfterCooldown);
            return reduction;
        });

        player.server.tell(new TickTask(
            player.server.getTickCount(),
            () -> {
                pending.complete(0.0F, 0.0F);
                discardPendingHealthRestore(pending);
            }
        ));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Deque<PendingDamage> pendingRestores = PENDING_HEALTH_RESTORES.get(player);
        if (pendingRestores == null) {
            return;
        }

        Iterator<PendingDamage> iterator = pendingRestores.iterator();
        while (iterator.hasNext()) {
            PendingDamage pending = iterator.next();
            if (pending.source == event.getSource()) {
                iterator.remove();
                player.setHealth(Math.min(
                    player.getMaxHealth(),
                    player.getHealth() + pending.actualDamage
                ));
                break;
            }
        }

        if (pendingRestores.isEmpty()) {
            PENDING_HEALTH_RESTORES.remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        copyBoolean(event, SELF_TESTER_MODE_TAG);
        copyBoolean(event, DAMAGE_IMMUNITY_TAG);
        copyBoolean(event, OUTPUT_ZERO_DAMAGE_TAG);
    }

    private static void copyBoolean(PlayerEvent.Clone event, String key) {
        if (event.getOriginal().getPersistentData().contains(key)) {
            event.getEntity().getPersistentData().putBoolean(
                key,
                event.getOriginal().getPersistentData().getBoolean(key)
            );
        }
    }

    private static void discardPendingHealthRestore(PendingDamage pending) {
        Deque<PendingDamage> pendingRestores = PENDING_HEALTH_RESTORES.get(pending.player);
        if (pendingRestores != null) {
            pendingRestores.remove(pending);
            if (pendingRestores.isEmpty()) {
                PENDING_HEALTH_RESTORES.remove(pending.player);
            }
        }
    }

    private static final class PendingDamage {

        private final ServerPlayer player;
        private final float originalDamage;
        private final DamageSource source;
        private final String damageTypeName;
        private final long gameTick;
        private final boolean outputZeroDamage;
        private final boolean preserveHealth;
        private float actualDamage;
        private boolean completed;

        private PendingDamage(ServerPlayer player, float originalDamage, DamageSource source, String damageTypeName,
                              long gameTick, boolean outputZeroDamage, boolean preserveHealth) {
            this.player = player;
            this.originalDamage = originalDamage;
            this.source = source;
            this.damageTypeName = damageTypeName;
            this.gameTick = gameTick;
            this.outputZeroDamage = outputZeroDamage;
            this.preserveHealth = preserveHealth;
        }

        private void complete(float actualDamage, float damageAfterCooldown) {
            if (this.completed) {
                return;
            }
            this.completed = true;

            if (actualDamage <= 0.0F && !this.outputZeroDamage) {
                return;
            }

            this.player.sendSystemMessage(Component.translatable(
                "chat.tester_mod.damage_info",
                this.player.getName().getString(),
                String.format(Locale.ROOT, "%.1f", actualDamage),
                String.format(Locale.ROOT, "%.1f", damageAfterCooldown),
                String.format(Locale.ROOT, "%.1f", this.originalDamage),
                this.damageTypeName,
                String.valueOf(this.gameTick)
            ));
        }
    }
}

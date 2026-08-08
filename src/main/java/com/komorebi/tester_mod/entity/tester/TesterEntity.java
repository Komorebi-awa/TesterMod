package com.komorebi.tester_mod.entity.tester;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TesterEntity extends Mob implements VillagerDataHolder {

    public static final double MIN_ARMOR = 0.0;
    public static final double MAX_ARMOR = 30.0;
    public static final double MIN_ARMOR_TOUGHNESS = 0.0;
    public static final double MAX_ARMOR_TOUGHNESS = 20.0;
    public static final double MIN_MAX_HEALTH = 1.0;
    public static final double MAX_MAX_HEALTH = 1024.0;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_KNOCKBACKABLE =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HIDE_ZERO_DAMAGE =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.BOOLEAN);

    private VillagerData villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);
    private final Deque<DamageReport> damageReports = new ArrayDeque<>();

    public static final ResourceKey<DamageType> REMOVE_TESTER =
        ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("tester_mod", "remove_tester"));

    public TesterEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 1000.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ARMOR_TOUGHNESS, 0.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_KNOCKBACKABLE, false);
        builder.define(DATA_HIDE_ZERO_DAMAGE, false);
    }

    public void setKnockbackable(boolean knockbackable) {
        this.entityData.set(DATA_KNOCKBACKABLE, knockbackable);
    }

    public boolean isKnockbackable() {
        return this.entityData.get(DATA_KNOCKBACKABLE);
    }

    public void setOutputZeroDamage(boolean outputZeroDamage) {
        this.entityData.set(DATA_HIDE_ZERO_DAMAGE, !outputZeroDamage);
    }

    public boolean shouldOutputZeroDamage() {
        return !this.entityData.get(DATA_HIDE_ZERO_DAMAGE);
    }

    public static boolean isValidConfiguration(double armor, double armorToughness, double maxHealth) {
        return Double.isFinite(armor)
            && Double.isFinite(armorToughness)
            && Double.isFinite(maxHealth)
            && armor >= MIN_ARMOR
            && armor <= MAX_ARMOR
            && armorToughness >= MIN_ARMOR_TOUGHNESS
            && armorToughness <= MAX_ARMOR_TOUGHNESS
            && maxHealth >= MIN_MAX_HEALTH
            && maxHealth <= MAX_MAX_HEALTH;
    }

    public void applyConfiguration(boolean knockbackable, boolean outputZeroDamage,
                                   double armor, double armorToughness, double maxHealth) {
        if (!isValidConfiguration(armor, armorToughness, maxHealth)) {
            return;
        }

        this.setKnockbackable(knockbackable);
        this.setOutputZeroDamage(outputZeroDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(armorToughness);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public VillagerData getVillagerData() {
        return this.villagerData;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        this.villagerData = data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        getOwnerUUID().ifPresent(uuid -> compound.putUUID("Owner", uuid));
        compound.putBoolean("Knockbackable", isKnockbackable());
        compound.putBoolean("HideZeroDamage", !shouldOutputZeroDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(compound.getUUID("Owner")));
        }
        if (compound.contains("Knockbackable")) {
            this.entityData.set(DATA_KNOCKBACKABLE, compound.getBoolean("Knockbackable"));
        }
        if (compound.contains("HideZeroDamage")) {
            this.entityData.set(DATA_HIDE_ZERO_DAMAGE, compound.getBoolean("HideZeroDamage"));
        }
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && random.nextFloat() < 0.05f) {
            level().addParticle(ParticleTypes.INSTANT_EFFECT,
                getX() + (random.nextDouble() - 0.5) * 0.5,
                getY() + random.nextDouble() * 1.5,
                getZ() + (random.nextDouble() - 0.5) * 0.5,
                0, 0, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(REMOVE_TESTER)) {
            if (!level().isClientSide()) {
                this.remove(Entity.RemovalReason.KILLED);
            }
            return true;
        }

        if (!level().isClientSide()
            && source.getEntity() instanceof Player attacker
            && attacker.getMainHandItem().is(ModItems.TESTER_SETTER.get())
            && getOwnerUUID().filter(attacker.getUUID()::equals).isPresent()) {
            DamageSource removeSource = new DamageSource(
                level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(REMOVE_TESTER),
                attacker
            );
            return this.hurt(removeSource, Float.MAX_VALUE);
        }

        if (source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }

        DamageReport report = new DamageReport(amount);
        this.damageReports.push(report);
        boolean hurtResult;
        try {
            hurtResult = super.hurt(source, amount);
        } finally {
            this.damageReports.pop();
        }

        if (!this.level().isClientSide()
            && (this.shouldOutputZeroDamage() || report.damageAfterCooldown > 0.0F)) {
            this.sendDamageReport(source, report);
        }
        return hurtResult;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        DamageReport report = this.damageReports.peek();
        if (report != null) {
            report.damageAfterCooldown = amount;
        }

        float previousHealth = this.getHealth();
        float previousAbsorption = this.getAbsorptionAmount();
        try {
            super.actuallyHurt(source, amount);
            if (report != null) {
                report.actualDamage = Math.max(0.0F, previousHealth - this.getHealth());
            }
        } finally {
            this.setHealth(previousHealth);
            this.setAbsorptionAmount(previousAbsorption);
        }
    }

    private void sendDamageReport(DamageSource source, DamageReport report) {
        getOwnerUUID().ifPresent(uuid -> {
            Player player = level().getPlayerByUUID(uuid);
            if (player != null) {
                player.sendSystemMessage(Component.translatable(
                    "chat.tester_mod.damage_info",
                    Component.literal(this.getName().getString()).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.format(Locale.ROOT, "%.1f", report.actualDamage)).withStyle(ChatFormatting.RED),
                    Component.literal(String.format(Locale.ROOT, "%.1f", report.damageAfterCooldown)).withStyle(ChatFormatting.RED),
                    Component.literal(String.format(Locale.ROOT, "%.1f", report.originalDamage)).withStyle(ChatFormatting.RED),
                    Component.literal(source.getMsgId()).withStyle(ChatFormatting.RED),
                    Component.literal(String.valueOf(level().getGameTime())).withStyle(ChatFormatting.GREEN)    
                ));
            }
        });
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (this.isKnockbackable()) {
            super.knockback(strength, x, z);
        }
    }

    @Override
    public void markHurt() {
        if (this.isKnockbackable()) {
            super.markHurt();
        }
    }

    @Override
    public boolean isPushable() {
        return this.isKnockbackable();
    }

    @Override
    public boolean isPushedByFluid() {
        return this.isKnockbackable();
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        if (this.isKnockbackable()) {
            super.doPush(entity);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    private static final class DamageReport {

        private final float originalDamage;
        private float damageAfterCooldown;
        private float actualDamage;

        private DamageReport(float originalDamage) {
            this.originalDamage = originalDamage;
        }
    }
}

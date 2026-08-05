package com.komorebi.tester_mod.network;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateTesterConfigPayload(
    int entityId,
    boolean knockbackable,
    boolean outputZeroDamage,
    double armor,
    double armorToughness,
    double maxHealth
) implements CustomPacketPayload {

    public static final Type<UpdateTesterConfigPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "update_tester_config")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTesterConfigPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public UpdateTesterConfigPayload decode(RegistryFriendlyByteBuf buffer) {
                return new UpdateTesterConfigPayload(
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, UpdateTesterConfigPayload payload) {
                buffer.writeVarInt(payload.entityId());
                buffer.writeBoolean(payload.knockbackable());
                buffer.writeBoolean(payload.outputZeroDamage());
                buffer.writeDouble(payload.armor());
                buffer.writeDouble(payload.armorToughness());
                buffer.writeDouble(payload.maxHealth());
            }
        };

    public static void handle(UpdateTesterConfigPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof TesterEntity tester)
            || player.distanceToSqr(tester) > 64.0
            || !isHoldingTesterSetter(player)
            || !TesterEntity.isValidConfiguration(
                payload.armor(), payload.armorToughness(), payload.maxHealth())) {
            return;
        }

        tester.applyConfiguration(
            payload.knockbackable(),
            payload.outputZeroDamage(),
            payload.armor(),
            payload.armorToughness(),
            payload.maxHealth()
        );
        player.displayClientMessage(Component.translatable("chat.tester_mod.configuration_applied"), true);
    }

    private static boolean isHoldingTesterSetter(ServerPlayer player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.TESTER_SETTER.get())
            || player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.TESTER_SETTER.get());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

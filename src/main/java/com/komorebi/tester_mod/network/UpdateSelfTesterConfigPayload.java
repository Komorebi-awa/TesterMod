package com.komorebi.tester_mod.network;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.event.SelfTesterHandler;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSelfTesterConfigPayload(
    boolean damageImmunity,
    boolean outputZeroDamage
) implements CustomPacketPayload {

    public static final Type<UpdateSelfTesterConfigPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "update_self_tester_config")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSelfTesterConfigPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public UpdateSelfTesterConfigPayload decode(RegistryFriendlyByteBuf buffer) {
                return new UpdateSelfTesterConfigPayload(buffer.readBoolean(), buffer.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, UpdateSelfTesterConfigPayload payload) {
                buffer.writeBoolean(payload.damageImmunity());
                buffer.writeBoolean(payload.outputZeroDamage());
            }
        };

    public static void handle(UpdateSelfTesterConfigPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
            || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
            || !SelfTesterHandler.isEnabled(player)
            || !isHoldingTesterSetter(player)) {
            return;
        }

        SelfTesterHandler.applyConfiguration(
            player,
            payload.damageImmunity(),
            payload.outputZeroDamage()
        );
        player.displayClientMessage(Component.translatable(
            "chat.tester_mod.self_tester.configuration_applied"
        ), true);
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

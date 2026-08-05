package com.komorebi.tester_mod.network;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.client.ClientSelfTesterConfigHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenSelfTesterConfigPayload(
    boolean damageImmunity,
    boolean outputZeroDamage
) implements CustomPacketPayload {

    public static final Type<OpenSelfTesterConfigPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "open_self_tester_config")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSelfTesterConfigPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public OpenSelfTesterConfigPayload decode(RegistryFriendlyByteBuf buffer) {
                return new OpenSelfTesterConfigPayload(buffer.readBoolean(), buffer.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, OpenSelfTesterConfigPayload payload) {
                buffer.writeBoolean(payload.damageImmunity());
                buffer.writeBoolean(payload.outputZeroDamage());
            }
        };

    public static void handle(OpenSelfTesterConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSelfTesterConfigHandler.open(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

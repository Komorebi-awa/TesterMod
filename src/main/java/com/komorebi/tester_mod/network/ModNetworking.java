package com.komorebi.tester_mod.network;

import com.komorebi.tester_mod.ModMain;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModMain.MODID)
public final class ModNetworking {

    private ModNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            UpdateTesterConfigPayload.TYPE,
            UpdateTesterConfigPayload.STREAM_CODEC,
            UpdateTesterConfigPayload::handle
        );
        registrar.playToClient(
            OpenSelfTesterConfigPayload.TYPE,
            OpenSelfTesterConfigPayload.STREAM_CODEC,
            OpenSelfTesterConfigPayload::handle
        );
        registrar.playToServer(
            UpdateSelfTesterConfigPayload.TYPE,
            UpdateSelfTesterConfigPayload.STREAM_CODEC,
            UpdateSelfTesterConfigPayload::handle
        );
    }
}

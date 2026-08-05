package com.komorebi.tester_mod.client;

import com.komorebi.tester_mod.client.screen.SelfTesterConfigScreen;
import com.komorebi.tester_mod.network.OpenSelfTesterConfigPayload;
import net.minecraft.client.Minecraft;

public final class ClientSelfTesterConfigHandler {

    private ClientSelfTesterConfigHandler() {
    }

    public static void open(OpenSelfTesterConfigPayload payload) {
        Minecraft.getInstance().setScreen(new SelfTesterConfigScreen(
            payload.damageImmunity(),
            payload.outputZeroDamage()
        ));
    }
}

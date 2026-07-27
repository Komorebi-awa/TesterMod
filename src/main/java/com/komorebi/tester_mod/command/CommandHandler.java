package com.komorebi.tester_mod.command;

import com.komorebi.tester_mod.ModMain;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ModMain.MODID)
public class CommandHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RemoveTesterCommand.register(event.getDispatcher());
    }
}

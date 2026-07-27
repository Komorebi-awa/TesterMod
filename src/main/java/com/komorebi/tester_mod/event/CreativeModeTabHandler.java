package com.komorebi.tester_mod.event;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = ModMain.MODID)
public class CreativeModeTabHandler {

    @SubscribeEvent
    public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.TESTER_SETTER.get());
        }
    }
}
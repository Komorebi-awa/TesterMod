package com.komorebi.tester_mod;

import com.komorebi.tester_mod.entity.ModEntities;
import com.komorebi.tester_mod.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "tester_mod";

    public ModMain(IEventBus modEventBus) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
    }
}

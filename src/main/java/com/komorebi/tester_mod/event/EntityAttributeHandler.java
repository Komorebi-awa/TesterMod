package com.komorebi.tester_mod.event;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.entity.ModEntities;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = ModMain.MODID)
public class EntityAttributeHandler {

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TESTER.get(), TesterEntity.createAttributes().build());
    }
}

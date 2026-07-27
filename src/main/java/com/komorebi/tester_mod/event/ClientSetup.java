package com.komorebi.tester_mod.event;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.entity.ModEntities;
import com.komorebi.tester_mod.entity.tester.TesterRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TESTER.get(), TesterRenderer::new);
    }
}

package com.komorebi.tester_mod.client;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public final class ClientTesterShieldHandler {

    private static final float USE_SLOWDOWN_COMPENSATION = 5.0F;

    private ClientTesterShieldHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!event.getEntity().isUsingItem()
            || !event.getEntity().getUseItem().is(ModItems.TESTER_SHIELD.get())) {
            return;
        }

        Input input = event.getInput();
        input.leftImpulse *= USE_SLOWDOWN_COMPENSATION;
        input.forwardImpulse *= USE_SLOWDOWN_COMPENSATION;
    }
}

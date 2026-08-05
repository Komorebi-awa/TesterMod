package com.komorebi.tester_mod.client;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.client.screen.TesterConfigScreen;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public final class ClientTesterInteractionHandler {

    private ClientTesterInteractionHandler() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()
            && event.getTarget() instanceof TesterEntity tester
            && event.getItemStack().is(ModItems.TESTER_SETTER.get())) {
            Minecraft minecraft = Minecraft.getInstance();
            int testerId = tester.getId();
            minecraft.tell(() -> {
                if (minecraft.level != null
                    && minecraft.level.getEntity(testerId) instanceof TesterEntity clientTester) {
                    minecraft.setScreen(new TesterConfigScreen(clientTester));
                }
            });
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}

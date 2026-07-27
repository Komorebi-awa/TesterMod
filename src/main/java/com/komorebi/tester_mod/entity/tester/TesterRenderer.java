package com.komorebi.tester_mod.entity.tester;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;

public class TesterRenderer extends MobRenderer<TesterEntity, VillagerModel<TesterEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");

    public TesterRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
    }

    @Override
    public ResourceLocation getTextureLocation(TesterEntity entity) {
        return TEXTURE;
    }
}

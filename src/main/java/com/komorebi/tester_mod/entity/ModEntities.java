package com.komorebi.tester_mod.entity;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, ModMain.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TesterEntity>> TESTER =
        ENTITY_TYPES.register("tester",
            () -> EntityType.Builder.of(
                    TesterEntity::new,
                    MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .eyeHeight(1.62f)
                .clientTrackingRange(10)
                .build("tester"));
}

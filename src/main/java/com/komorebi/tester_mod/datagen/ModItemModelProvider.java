package com.komorebi.tester_mod.datagen;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModMain.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.TESTER_SETTER.get());
    }
}
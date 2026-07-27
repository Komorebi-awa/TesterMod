package com.komorebi.tester_mod.item;

import com.komorebi.tester_mod.ModMain;
import com.komorebi.tester_mod.item.tester.TesterSetterItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModMain.MODID);

    public static final DeferredItem<Item> TESTER_SETTER = ITEMS.register("tester_setter",
        () -> new TesterSetterItem(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.UNCOMMON)));

    public static <T extends Item> DeferredItem<T> registerItem(String name, DeferredItem<T> item) {
        return item;
    }
}

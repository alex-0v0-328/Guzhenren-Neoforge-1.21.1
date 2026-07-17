package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.HopeGuItem;
import com.unknown.guzhenren.item.PrimevalStoneItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Every 蛊虫 and 蛊材. An id is also its texture's file name -- see datagen/item/ModItemModelProvider.
//  ⚠ 64 is Item.Properties' own default; a stacksTo(64) here would only be noise.
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Guzhenren.MOD_ID);

    //  真元 a stone gives back. Lives here: the number is what registration chose, not what the class is.
    private static final long PRIMEVAL_STONE_ESSENCE = 20L;

    //  一转人道蛊虫. 开窍 once, then refuses -- see CLAUDE.md "The awakening gate".
    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
            () -> new HopeGuItem(new Item.Properties()));

    //  一转天道蛊材.
    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

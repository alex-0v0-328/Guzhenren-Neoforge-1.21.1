package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.item.mortal.HopeGuItem;
import com.unknown.guzhenren.item.mortal.RelicsGuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Every Gu (蛊虫) and Gu Material (蛊材). An id is also its texture's file name -- see ModItemModelProvider.
//  ⚠ 64 is Item.Properties' own default; a stacksTo(64) here would only be noise.
//  TODO(data comp): stateful items (力道 school+magnitude, 蛊材 qi type+amount) need a ModDataComponents registry.
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Guzhenren.MOD_ID);

    //  Essence a stone gives back. Lives here: the number is what registration chose, not what the class is.
    private static final long PRIMEVAL_STONE_ESSENCE = 20L;

    //  Rank I, Human Path. Awakens once, then refuses -- see CLAUDE.md "The awakening gate".
    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
            () -> new HopeGuItem(new Item.Properties()));

    //  Rank I, Heaven Path.
    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

    //  Relics Gu (舍利蛊), one per rank 1..5 -- one class; the rank is all registration varies.
    //  ⚠ Named for that rank's essence color, but the strings do not transfer -- see CLAUDE.md "Items".
    public static final DeferredItem<Item> COPPER_RELICS_GU = ITEMS.register("copper_relics_gu",
            () -> new RelicsGuItem(new Item.Properties(), Rank.ONE));
    public static final DeferredItem<Item> STEEL_RELICS_GU = ITEMS.register("steel_relics_gu",
            () -> new RelicsGuItem(new Item.Properties(), Rank.TWO));
    public static final DeferredItem<Item> SILVER_RELICS_GU = ITEMS.register("silver_relics_gu",
            () -> new RelicsGuItem(new Item.Properties(), Rank.THREE));
    public static final DeferredItem<Item> GOLD_RELICS_GU = ITEMS.register("gold_relics_gu",
            () -> new RelicsGuItem(new Item.Properties(), Rank.FOUR));
    public static final DeferredItem<Item> CRYSTAL_RELICS_GU = ITEMS.register("crystal_relics_gu",
            () -> new RelicsGuItem(new Item.Properties(), Rank.FIVE));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

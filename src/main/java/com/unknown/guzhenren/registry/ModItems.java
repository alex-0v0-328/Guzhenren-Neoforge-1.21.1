package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.item.material.LiquorItem;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.item.mortal.strength.BoarGuItem;
import com.unknown.guzhenren.item.mortal.HopeGuItem;
import com.unknown.guzhenren.item.mortal.LifespanGuItem;
import com.unknown.guzhenren.item.mortal.liquor.LiquorWormItem;
import com.unknown.guzhenren.item.mortal.strength.HumanStrengthGuItem;
import com.unknown.guzhenren.item.mortal.RelicsGuItem;
import com.unknown.guzhenren.item.mortal.VitalityLeafGuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Every Gu [蛊虫] and Gu Material [蛊材]. An id is also its texture's file name -- see ModItemModelProvider.
//  ⚠ 64 is Item.Properties' own default; a stacksTo(64) here would only be noise. A stateful Gu says stacksTo(1).
//     TODO(data comp): a Gu material carrying a qi type + amount still needs its own component.
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Guzhenren.MOD_ID);

    //  Essence a stone gives back. Lives here: the number is what registration chose, not what the class is.
    private static final long PRIMEVAL_STONE_ESSENCE = 20L;

    //  Rank I, Human Path. Awakens once, then refuses --  CLAUDE.md "The awakening gate".
    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
            () -> new HopeGuItem(new Item.Properties()));

    //  Rank I, Heaven Path.
    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

    //  Relics Gu [舍利蛊], one per rank 1..5 -- one class; the rank is all registration varies.
    //  ⚠ Named for that rank's essence color, but the strings do not transfer --  CLAUDE.md "Items".
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

    //  Boar Gu [豕蛊], one per beast -- one class; the beast is all registration varies.
    //  ⚠ stacksTo(1): each carries its own refinement and hunger, and a stack would share one component.
    public static final DeferredItem<Item> WHITE_BOAR_GU = ITEMS.register("white_boar_gu",
            () -> new BoarGuItem(new Item.Properties().stacksTo(1), BeastStrength.WHITE_BOAR));
    public static final DeferredItem<Item> BLACK_BOAR_GU = ITEMS.register("black_boar_gu",
            () -> new BoarGuItem(new Item.Properties().stacksTo(1), BeastStrength.BLACK_BOAR));

    //  Human Jun Strength Branch [人力钧力流] ×4, Ranks I-IV -- one class; the rank and Jun kind vary.
    //  18 uses buy one of nine layers.  ⚠ stacksTo(1): each carries its own refinement and hunger.
    public static final DeferredItem<Item> JIN_STRENGTH_GU = ITEMS.register("jin_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.ONE, HumanStrength.JIN));
    public static final DeferredItem<Item> TENS_JIN_STRENGTH_GU = ITEMS.register("tens_jin_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.TWO, HumanStrength.TEN_JIN));
    public static final DeferredItem<Item> JUN_STRENGTH_GU = ITEMS.register("jun_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.THREE, HumanStrength.JUN));
    public static final DeferredItem<Item> TENS_JUN_STRENGTH_GU = ITEMS.register("tens_jun_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.FOUR, HumanStrength.TEN_JUN));

    //  Vitality Leaf Gu [生机叶蛊], Rank I Wood Path -- stacks freely; it carries no per-stack state.
    public static final DeferredItem<Item> VITALITY_LEAF_GU = ITEMS.register("vitality_leaf_gu",
            () -> new VitalityLeafGuItem(new Item.Properties()));

    //  Lifespan Gu [寿蛊] ×4, Rank I Heaven Path -- one class; the span is all registration varies.
    //  ⚠ The four ranges are decimal magnitudes, but they are passed EXPLICITLY, not derived from a
    //  tier -- a fifth need not be 10000..99999.
    public static final DeferredItem<Item> LIFESPAN_GU = ITEMS.register("lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 1, 9));
    public static final DeferredItem<Item> TENS_LIFESPAN_GU = ITEMS.register("tens_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 10, 99));
    public static final DeferredItem<Item> HUNDREDS_LIFESPAN_GU = ITEMS.register("hundreds_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 100, 999));
    public static final DeferredItem<Item> THOUSANDS_LIFESPAN_GU = ITEMS.register("thousands_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 1000, 9999));

    //  Liquor Worm [酒虫], one per rank I..IV -- one class; the rank is all registration varies, and
    //  every number it needs falls out of that.
    //  ⚠ Usable only at its OWN rank. Below or above, it can still be refined but never driven.
    public static final DeferredItem<Item> LIQUOR_WORM = ITEMS.register("liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.ONE));
    public static final DeferredItem<Item> FOUR_FLAVORS_LIQUOR_WORM = ITEMS.register("four_flavors_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.TWO));
    public static final DeferredItem<Item> SEVEN_FRAGRANCES_LIQUOR_WORM = ITEMS.register(
            "seven_fragrances_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.THREE));
    public static final DeferredItem<Item> NINE_EYES_LIQUOR_WORM = ITEMS.register("nine_eyes_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.FOUR));

    //  Liquor [酒], Rank I Food Path -- what every liquor worm drinks, and drinkable itself: 60% nausea.
    //  ⚠ Stacks: the food is Properties data, so nothing per-stack rides on it.
    public static final DeferredItem<Item> LIQUOR = ITEMS.register("liquor",
            () -> new LiquorItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

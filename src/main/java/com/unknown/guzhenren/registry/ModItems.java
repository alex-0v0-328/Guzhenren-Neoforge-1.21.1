package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.item.material.LiquorItem;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.item.material.qi.DeathQiItem;
import com.unknown.guzhenren.item.material.qi.EssenceQiItem;
import com.unknown.guzhenren.item.material.qi.LifeQiItem;
import com.unknown.guzhenren.item.material.qi.QiMaterialItem;
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

    //region Qi Path [气道] materials -- 21, ranks I..V
    //  Hold to refine, and the moment it finishes it is used and gone. ⚠ They stack to 64: unlike a
    //  RefinableGuItem there is no per-stack state, which is exactly why they are not one.
    //  Every kind pays specks under its own tag (1/4/16/64/256 by rank); three of them leave an effect.
    //  ⚠ Sword and Strength Qi register the BASE class -- specks are all they do, so no leaf earns its keep.
    public static final DeferredItem<Item> FIRST_SWORD_QI = qiMaterial("first_sword_qi", Rank.ONE, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> SECOND_SWORD_QI = qiMaterial("second_sword_qi", Rank.TWO, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> THIRD_SWORD_QI = qiMaterial("third_sword_qi", Rank.THREE, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> FOURTH_SWORD_QI = qiMaterial("fourth_sword_qi", Rank.FOUR, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> FIFTH_SWORD_QI = qiMaterial("fifth_sword_qi", Rank.FIVE, MarkTag.QI_SWORD);

    public static final DeferredItem<Item> FIRST_STRENGTH_QI =
            qiMaterial("first_strength_qi", Rank.ONE, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> SECOND_STRENGTH_QI =
            qiMaterial("second_strength_qi", Rank.TWO, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> THIRD_STRENGTH_QI =
            qiMaterial("third_strength_qi", Rank.THREE, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> FOURTH_STRENGTH_QI =
            qiMaterial("fourth_strength_qi", Rank.FOUR, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> FIFTH_STRENGTH_QI =
            qiMaterial("fifth_strength_qi", Rank.FIVE, MarkTag.QI_STRENGTH);

    //  Life Qi [生气]: health regeneration, and the one thing that cures Death Qi.
    public static final DeferredItem<Item> FIRST_LIFE_QI = ITEMS.register("first_life_qi",
            () -> new LifeQiItem(qiProperties(), Rank.ONE));
    public static final DeferredItem<Item> SECOND_LIFE_QI = ITEMS.register("second_life_qi",
            () -> new LifeQiItem(qiProperties(), Rank.TWO));
    public static final DeferredItem<Item> THIRD_LIFE_QI = ITEMS.register("third_life_qi",
            () -> new LifeQiItem(qiProperties(), Rank.THREE));
    public static final DeferredItem<Item> FOURTH_LIFE_QI = ITEMS.register("fourth_life_qi",
            () -> new LifeQiItem(qiProperties(), Rank.FOUR));
    public static final DeferredItem<Item> FIFTH_LIFE_QI = ITEMS.register("fifth_life_qi",
            () -> new LifeQiItem(qiProperties(), Rank.FIVE));

    //  Essence Qi [元气]: faster essence regen for a minute; the rank buys the rate, not the time.
    public static final DeferredItem<Item> FIRST_ESSENCE_QI = ITEMS.register("first_essence_qi",
            () -> new EssenceQiItem(qiProperties(), Rank.ONE));
    public static final DeferredItem<Item> SECOND_ESSENCE_QI = ITEMS.register("second_essence_qi",
            () -> new EssenceQiItem(qiProperties(), Rank.TWO));
    public static final DeferredItem<Item> THIRD_ESSENCE_QI = ITEMS.register("third_essence_qi",
            () -> new EssenceQiItem(qiProperties(), Rank.THREE));
    public static final DeferredItem<Item> FOURTH_ESSENCE_QI = ITEMS.register("fourth_essence_qi",
            () -> new EssenceQiItem(qiProperties(), Rank.FOUR));
    public static final DeferredItem<Item> FIFTH_ESSENCE_QI = ITEMS.register("fifth_essence_qi",
            () -> new EssenceQiItem(qiProperties(), Rank.FIVE));

    //  ⚠ Death Qi [死气] exists at Rank V ALONE -- there is no ladder, so nothing about it scales.
    public static final DeferredItem<Item> FIFTH_DEATH_QI = ITEMS.register("fifth_death_qi",
            () -> new DeathQiItem(qiProperties(), Rank.FIVE));

    private static DeferredItem<Item> qiMaterial(String id, Rank rank, MarkTag tag) {
        return ITEMS.register(id, () -> new QiMaterialItem(qiProperties(), rank, tag));
    }

    private static Item.Properties qiProperties() {return new Item.Properties().stacksTo(64);}
    //endregion

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

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
import com.unknown.guzhenren.item.mortal.strength.AllOutEffortGuItem;
import com.unknown.guzhenren.item.mortal.strength.BoarGuItem;
import com.unknown.guzhenren.item.mortal.HopeGuItem;
import com.unknown.guzhenren.item.mortal.LifespanGuItem;
import com.unknown.guzhenren.item.mortal.PrimevalElderGuItem;
import com.unknown.guzhenren.item.mortal.liquor.LiquorWormItem;
import com.unknown.guzhenren.item.mortal.strength.FlowerBoarGuItem;
import com.unknown.guzhenren.item.mortal.strength.HumanStrengthGuItem;
import com.unknown.guzhenren.item.mortal.RelicsGuItem;
import com.unknown.guzhenren.item.mortal.VitalityLeafGuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//     TODO(data comp): a Gu material carrying a qi type + amount still needs its own component.
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Guzhenren.MOD_ID);

    private static final long PRIMEVAL_STONE_ESSENCE = 20L;

    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
            () -> new HopeGuItem(new Item.Properties()));

    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

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

    public static final DeferredItem<Item> WHITE_BOAR_GU = ITEMS.register("white_boar_gu",
            () -> new BoarGuItem(new Item.Properties().stacksTo(1), BeastStrength.WHITE_BOAR));
    public static final DeferredItem<Item> BLACK_BOAR_GU = ITEMS.register("black_boar_gu",
            () -> new BoarGuItem(new Item.Properties().stacksTo(1), BeastStrength.BLACK_BOAR));
    public static final DeferredItem<Item> FLOWER_BOAR_GU = ITEMS.register("flower_boar_gu",
            () -> new FlowerBoarGuItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> JIN_STRENGTH_GU = ITEMS.register("jin_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.ONE, HumanStrength.JIN));
    public static final DeferredItem<Item> TENS_JIN_STRENGTH_GU = ITEMS.register("tens_jin_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.TWO, HumanStrength.TEN_JIN));
    public static final DeferredItem<Item> JUN_STRENGTH_GU = ITEMS.register("jun_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.THREE, HumanStrength.JUN));
    public static final DeferredItem<Item> TENS_JUN_STRENGTH_GU = ITEMS.register("tens_jun_strength_gu",
            () -> new HumanStrengthGuItem(new Item.Properties().stacksTo(1), Rank.FOUR, HumanStrength.TEN_JUN));

    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_3 = ITEMS.register("all_out_effort_gu_3",
            () -> new AllOutEffortGuItem(new Item.Properties().stacksTo(1), Rank.THREE));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_4 = ITEMS.register("all_out_effort_gu_4",
            () -> new AllOutEffortGuItem(new Item.Properties().stacksTo(1), Rank.FOUR));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_5 = ITEMS.register("all_out_effort_gu_5",
            () -> new AllOutEffortGuItem(new Item.Properties().stacksTo(1), Rank.FIVE));

    public static final DeferredItem<Item> VITALITY_LEAF_GU = ITEMS.register("vitality_leaf_gu",
            () -> new VitalityLeafGuItem(new Item.Properties()));

    public static final DeferredItem<Item> LIFESPAN_GU = ITEMS.register("lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 1, 9));
    public static final DeferredItem<Item> TENS_LIFESPAN_GU = ITEMS.register("tens_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 10, 19));
    public static final DeferredItem<Item> HUNDREDS_LIFESPAN_GU = ITEMS.register("hundreds_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 100, 199));
    public static final DeferredItem<Item> THOUSANDS_LIFESPAN_GU = ITEMS.register("thousands_lifespan_gu",
            () -> new LifespanGuItem(new Item.Properties(), 1000, 1999));

    public static final DeferredItem<Item> LIQUOR_WORM = ITEMS.register("liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.ONE));
    public static final DeferredItem<Item> FOUR_FLAVORS_LIQUOR_WORM = ITEMS.register("four_flavors_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.TWO));
    public static final DeferredItem<Item> SEVEN_FRAGRANCES_LIQUOR_WORM = ITEMS.register(
            "seven_fragrances_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.THREE));
    public static final DeferredItem<Item> NINE_EYES_LIQUOR_WORM = ITEMS.register("nine_eyes_liquor_worm",
            () -> new LiquorWormItem(new Item.Properties().stacksTo(1), Rank.FOUR));

    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_1 = ITEMS.register("primeval_elder_gu_1",
            () -> new PrimevalElderGuItem(new Item.Properties().stacksTo(1), Rank.ONE));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_2 = ITEMS.register("primeval_elder_gu_2",
            () -> new PrimevalElderGuItem(new Item.Properties().stacksTo(1), Rank.TWO));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_3 = ITEMS.register("primeval_elder_gu_3",
            () -> new PrimevalElderGuItem(new Item.Properties().stacksTo(1), Rank.THREE));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_4 = ITEMS.register("primeval_elder_gu_4",
            () -> new PrimevalElderGuItem(new Item.Properties().stacksTo(1), Rank.FOUR));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_5 = ITEMS.register("primeval_elder_gu_5",
            () -> new PrimevalElderGuItem(new Item.Properties().stacksTo(1), Rank.FIVE));

    public static final DeferredItem<Item> LIQUOR = ITEMS.register("liquor",
            () -> new LiquorItem(new Item.Properties()));

    //region Qi Path [气道] materials -- 21, ranks I..V
    public static final DeferredItem<Item> SWORD_QI_1 = qiMaterial("sword_qi_1", Rank.ONE, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> SWORD_QI_2 = qiMaterial("sword_qi_2", Rank.TWO, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> SWORD_QI_3 = qiMaterial("sword_qi_3", Rank.THREE, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> SWORD_QI_4 = qiMaterial("sword_qi_4", Rank.FOUR, MarkTag.QI_SWORD);
    public static final DeferredItem<Item> SWORD_QI_5 = qiMaterial("sword_qi_5", Rank.FIVE, MarkTag.QI_SWORD);

    public static final DeferredItem<Item> STRENGTH_QI_1 = qiMaterial("strength_qi_1", Rank.ONE, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_2 = qiMaterial("strength_qi_2", Rank.TWO, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_3 = qiMaterial("strength_qi_3", Rank.THREE, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_4 = qiMaterial("strength_qi_4", Rank.FOUR, MarkTag.QI_STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_5 = qiMaterial("strength_qi_5", Rank.FIVE, MarkTag.QI_STRENGTH);

    public static final DeferredItem<Item> LIFE_QI_1 = ITEMS.register("life_qi_1",
            () -> new LifeQiItem(qiProperties(), Rank.ONE));
    public static final DeferredItem<Item> LIFE_QI_2 = ITEMS.register("life_qi_2",
            () -> new LifeQiItem(qiProperties(), Rank.TWO));
    public static final DeferredItem<Item> LIFE_QI_3 = ITEMS.register("life_qi_3",
            () -> new LifeQiItem(qiProperties(), Rank.THREE));
    public static final DeferredItem<Item> LIFE_QI_4 = ITEMS.register("life_qi_4",
            () -> new LifeQiItem(qiProperties(), Rank.FOUR));
    public static final DeferredItem<Item> LIFE_QI_5 = ITEMS.register("life_qi_5",
            () -> new LifeQiItem(qiProperties(), Rank.FIVE));

    public static final DeferredItem<Item> ESSENCE_QI_1 = ITEMS.register("essence_qi_1",
            () -> new EssenceQiItem(qiProperties(), Rank.ONE));
    public static final DeferredItem<Item> ESSENCE_QI_2 = ITEMS.register("essence_qi_2",
            () -> new EssenceQiItem(qiProperties(), Rank.TWO));
    public static final DeferredItem<Item> ESSENCE_QI_3 = ITEMS.register("essence_qi_3",
            () -> new EssenceQiItem(qiProperties(), Rank.THREE));
    public static final DeferredItem<Item> ESSENCE_QI_4 = ITEMS.register("essence_qi_4",
            () -> new EssenceQiItem(qiProperties(), Rank.FOUR));
    public static final DeferredItem<Item> ESSENCE_QI_5 = ITEMS.register("essence_qi_5",
            () -> new EssenceQiItem(qiProperties(), Rank.FIVE));

    public static final DeferredItem<Item> DEATH_QI_5 = ITEMS.register("death_qi_5",
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

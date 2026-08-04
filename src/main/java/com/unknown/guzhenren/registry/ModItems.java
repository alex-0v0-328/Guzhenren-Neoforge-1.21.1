package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.effect.BruteForceLonghornBeetleGuEffect;
import com.unknown.guzhenren.effect.DragonpillCricketGuEffect;
import com.unknown.guzhenren.effect.FlowerBoarGuEffect;
import com.unknown.guzhenren.item.GuSpec;
import com.unknown.guzhenren.item.material.LiquorItem;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.item.material.qi.DeathQiItem;
import com.unknown.guzhenren.item.material.qi.EssenceQiItem;
import com.unknown.guzhenren.item.material.qi.LifeQiItem;
import com.unknown.guzhenren.item.material.qi.QiMaterialItem;
import com.unknown.guzhenren.item.mortal.BuffGuItem;
import com.unknown.guzhenren.item.mortal.HopeGuItem;
import com.unknown.guzhenren.item.mortal.LifespanGuItem;
import com.unknown.guzhenren.item.mortal.PrimevalElderGuItem;
import com.unknown.guzhenren.item.mortal.RelicsGuItem;
import com.unknown.guzhenren.item.mortal.VitalityLeafGuItem;
import com.unknown.guzhenren.item.mortal.liquor.LiquorWormItem;
import com.unknown.guzhenren.item.mortal.strength.AllOutEffortGuItem;
import com.unknown.guzhenren.item.mortal.strength.BeastStrengthGuItem;
import com.unknown.guzhenren.item.mortal.strength.HumanStrengthGuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//     TODO(data comp): a Gu material carrying a qi type + amount still needs its own component.
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Guzhenren.MOD_ID);

    private static final long PRIMEVAL_STONE_ESSENCE = 20L;

    private static Item.Properties tended() {return new Item.Properties().stacksTo(1);}
    private static Item.Properties oneShot() {return new Item.Properties();}

    //region 一次性蛊虫 -- refining IS the use; one charged press pays, lands and spends
    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
            () -> new HopeGuItem(oneShot().stacksTo(1), GuSpec.of(Rank.ONE, GuPath.HUMAN)));

    public static final DeferredItem<Item> VITALITY_LEAF_GU = ITEMS.register("vitality_leaf_gu",
            () -> new VitalityLeafGuItem(oneShot(), GuSpec.of(Rank.ONE, GuPath.WOOD)));

    public static final DeferredItem<Item> LIFESPAN_GU = ITEMS.register("lifespan_gu",
            () -> new LifespanGuItem(oneShot(),    1,    9, GuSpec.of(Rank.ONE, GuPath.HEAVEN)));
    public static final DeferredItem<Item> TENS_LIFESPAN_GU = ITEMS.register("tens_lifespan_gu",
            () -> new LifespanGuItem(oneShot(),   10,   19, GuSpec.of(Rank.ONE, GuPath.HEAVEN)));
    public static final DeferredItem<Item> HUNDREDS_LIFESPAN_GU = ITEMS.register("hundreds_lifespan_gu",
            () -> new LifespanGuItem(oneShot(),  100,  199, GuSpec.of(Rank.ONE, GuPath.HEAVEN)));
    public static final DeferredItem<Item> THOUSANDS_LIFESPAN_GU = ITEMS.register("thousands_lifespan_gu",
            () -> new LifespanGuItem(oneShot(), 1000, 1999, GuSpec.of(Rank.ONE, GuPath.HEAVEN)));

    public static final DeferredItem<Item> COPPER_RELICS_GU = ITEMS.register("copper_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.ONE, GuPath.HEAVEN).refine(1)));
    public static final DeferredItem<Item> STEEL_RELICS_GU = ITEMS.register("steel_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.TWO, GuPath.HEAVEN).refine(10)));
    public static final DeferredItem<Item> SILVER_RELICS_GU = ITEMS.register("silver_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.THREE, GuPath.HEAVEN).refine(100)));
    public static final DeferredItem<Item> GOLD_RELICS_GU = ITEMS.register("gold_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.FOUR, GuPath.HEAVEN).refine(1_000)));
    public static final DeferredItem<Item> CRYSTAL_RELICS_GU = ITEMS.register("crystal_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.FIVE, GuPath.HEAVEN).refine(10_000)));
    //endregion

    //region 兽力虚影流 -- one round of 3,600 buys a beast's strength, held once ever
    public static final DeferredItem<Item> WHITE_BOAR_GU = ITEMS.register("white_boar_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.WHITE_BOAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_000)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.WHITE_BOAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BOAR_FEED, 1)));
    public static final DeferredItem<Item> BLACK_BOAR_GU = ITEMS.register("black_boar_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.BLACK_BOAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_000)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.BLACK_BOAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BOAR_FEED, 1)));
    public static final DeferredItem<Item> BEAR_STRENGTH_GU = ITEMS.register("bear_strength_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.BEAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_000)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.BEAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BEAR_FEED, 1)));
    //endregion

    //region 一转力道即时增益 -- one class, the effect and its length given at registration
    public static final DeferredItem<Item> FLOWER_BOAR_GU = ITEMS.register("flower_boar_gu",
            () -> new BuffGuItem(tended(), ModEffects.FLOWER_BOAR_GU, FlowerBoarGuEffect.DURATION_TICKS,
                    GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                            .refine(1_000)
                            .costPerUse(20)
                            .hungerBar(9, 3).hungerPerUse(3)
                            .feed(ModItemTags.BOAR_FEED, 1)
                            .cooldown(2 * Ticks.MINUTE)));
    public static final DeferredItem<Item> DRAGONPILL_CRICKET_GU = ITEMS.register("dragonpill_cricket_gu",
            () -> new BuffGuItem(tended(), ModEffects.DRAGONPILL_CRICKET_GU,
                    DragonpillCricketGuEffect.DURATION_TICKS,
                    GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                            .refine(1_000)
                            .costPerUse(20)
                            .hungerBar(8, 4).hungerPerUse(1)
                            .feed(ModItemTags.RABBIT_FEED, 1)
                            .cooldown(Ticks.MINUTE)));
    public static final DeferredItem<Item> BRUTE_FORCE_LONGHORN_BEETLE_GU =
            ITEMS.register("brute_force_longhorn_beetle_gu",
                    () -> new BuffGuItem(tended(), ModEffects.BRUTE_FORCE_LONGHORN_BEETLE_GU,
                            BruteForceLonghornBeetleGuEffect.DURATION_TICKS,
                            GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                                    .refine(1_000)
                                    .costPerUse(20)
                                    .hungerBar(8, 4).hungerPerUse(1)
                                    .feed(ModItemTags.BEEF_FEED, 1)
                                    .cooldown(Ticks.MINUTE)));
    //endregion

    //region 人力钧力流 -- one class, the kind at registration; one round is one layer
    public static final DeferredItem<Item> JIN_STRENGTH_GU = ITEMS.register("jin_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.JIN, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_000)
                    .channel(3_600)
                    .speckEvery(3_600, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 3).hungerEvery(200)
                    .feed(ModItemTags.JIN_FEED, 1)
                    .dense(ModItemTags.JIN_FEED_DENSE, 9)));
    public static final DeferredItem<Item> TENS_JIN_STRENGTH_GU = ITEMS.register("tens_jin_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.TEN_JIN, GuSpec.of(Rank.TWO, GuPath.STRENGTH)
                    .refine(10_000)
                    .channel(36_000)
                    .speckEvery(3_600, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 9).hungerEvery(2_000)
                    .feed(ModItemTags.JIN_FEED, 1)
                    .dense(ModItemTags.JIN_FEED_DENSE, 9)));
    public static final DeferredItem<Item> JUN_STRENGTH_GU = ITEMS.register("jun_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.JUN, GuSpec.of(Rank.THREE, GuPath.STRENGTH)
                    .refine(100_000)
                    .channel(72_000)
                    .speckEvery(2_400, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 9).hungerEvery(4_000)
                    .feed(ModItemTags.JIN_FEED_SMELTED, 1)
                    .dense(ModItemTags.JIN_FEED_SMELTED_DENSE, 9)));
    public static final DeferredItem<Item> TENS_JUN_STRENGTH_GU = ITEMS.register("tens_jun_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.TEN_JUN, GuSpec.of(Rank.FOUR, GuPath.STRENGTH)
                    .refine(1_000_000)
                    .channel(720_000)
                    .speckEvery(2_400, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 27).hungerEvery(40_000)
                    .feed(ModItemTags.JIN_FEED_SMELTED, 1)
                    .dense(ModItemTags.JIN_FEED_SMELTED_DENSE, 9)));
    //endregion

    //region 全力以赴蛊 -- 上古力道; the only thing that unlocks a stockpiled 9999 斤
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_3 = ITEMS.register("all_out_effort_gu_3",
            () -> new AllOutEffortGuItem(tended(), 60, GuSpec.of(Rank.THREE, GuPath.STRENGTH)
                    .refine(160_000)
                    .costPerUse(2_000)
                    .hungerBar(20, 8).hungerPerUse(5)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(80 * Ticks.SECOND)));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_4 = ITEMS.register("all_out_effort_gu_4",
            () -> new AllOutEffortGuItem(tended(), 90, GuSpec.of(Rank.FOUR, GuPath.STRENGTH)
                    .refine(1_600_000)
                    .costPerUse(20_000)
                    .hungerBar(20, 12).hungerPerUse(4)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(100 * Ticks.SECOND)));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_5 = ITEMS.register("all_out_effort_gu_5",
            () -> new AllOutEffortGuItem(tended(), 120, GuSpec.of(Rank.FIVE, GuPath.STRENGTH)
                    .refine(16_000_000)
                    .costPerUse(200_000)
                    .hungerBar(20, 16).hungerPerUse(2)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(120 * Ticks.SECOND)));
    //endregion

    //region 酒虫 -- the fed clock; one meal, two days of silence, flat at every rank
    public static final DeferredItem<Item> LIQUOR_WORM = ITEMS.register("liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.ONE, GuPath.FOOD)
                    .refine(1_600).costPerUse(20).fedClock(8).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> FOUR_FLAVORS_LIQUOR_WORM = ITEMS.register("four_flavors_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.TWO, GuPath.FOOD)
                    .refine(16_000).costPerUse(200).fedClock(16).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> SEVEN_FRAGRANCES_LIQUOR_WORM = ITEMS.register(
            "seven_fragrances_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.THREE, GuPath.FOOD)
                    .refine(160_000).costPerUse(2_000).fedClock(32).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> NINE_EYES_LIQUOR_WORM = ITEMS.register("nine_eyes_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.FOUR, GuPath.FOOD)
                    .refine(1_600_000).costPerUse(20_000).fedClock(64).feed(ModItemTags.LIQUOR_FEED, 1)));
    //endregion

    //region 元老蛊 -- a vault for 元石 that pays its own upkeep; eats by item, not by tag
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_1 = ITEMS.register("primeval_elder_gu_1",
            () -> new PrimevalElderGuItem(tended(), 1_000L, GuSpec.of(Rank.ONE, GuPath.SPACE)
                    .refine(10).costPerUse(1).fedClock(2).charge(20, 60, 100)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_2 = ITEMS.register("primeval_elder_gu_2",
            () -> new PrimevalElderGuItem(tended(), 10_000L, GuSpec.of(Rank.TWO, GuPath.SPACE)
                    .refine(100).costPerUse(1).fedClock(4).charge(20, 60, 100)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_3 = ITEMS.register("primeval_elder_gu_3",
            () -> new PrimevalElderGuItem(tended(), 100_000L, GuSpec.of(Rank.THREE, GuPath.SPACE)
                    .refine(1_000).costPerUse(1).fedClock(8).charge(20, 60, 100)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_4 = ITEMS.register("primeval_elder_gu_4",
            () -> new PrimevalElderGuItem(tended(), 1_000_000L, GuSpec.of(Rank.FOUR, GuPath.SPACE)
                    .refine(10_000).costPerUse(1).fedClock(16).charge(20, 60, 100)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_5 = ITEMS.register("primeval_elder_gu_5",
            () -> new PrimevalElderGuItem(tended(), 100_000_000L, GuSpec.of(Rank.FIVE, GuPath.SPACE)
                    .refine(100_000).costPerUse(1).fedClock(32).charge(20, 60, 100)));
    //endregion

    //region 蛊材 [Gu materials]
    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

    public static final DeferredItem<Item> LIQUOR = ITEMS.register("liquor",
            () -> new LiquorItem(new Item.Properties()));
    //endregion

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

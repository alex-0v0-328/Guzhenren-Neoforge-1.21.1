package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.effect.timed.BruteForceLonghornBeetleGuEffect;
import com.unknown.guzhenren.effect.timed.DragonpillCricketGuEffect;
import com.unknown.guzhenren.effect.timed.FlowerBoarGuEffect;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.mortal.BuffGuItem;
import com.unknown.guzhenren.item.gu.mortal.HopeGuItem;
import com.unknown.guzhenren.item.gu.mortal.LifespanGuItem;
import com.unknown.guzhenren.item.gu.mortal.PrimevalElderGuItem;
import com.unknown.guzhenren.item.gu.mortal.RelicsGuItem;
import com.unknown.guzhenren.item.gu.mortal.VitalityLeafGuItem;
import com.unknown.guzhenren.item.gu.mortal.liquor.LiquorWormItem;
import com.unknown.guzhenren.item.gu.mortal.soul.GutsGuItem;
import com.unknown.guzhenren.item.gu.mortal.strength.AllOutEffortGuItem;
import com.unknown.guzhenren.item.gu.mortal.strength.BeastStrengthGuItem;
import com.unknown.guzhenren.item.gu.mortal.strength.HumanStrengthGuItem;
import com.unknown.guzhenren.item.gu.mortal.time.WatchGuItem;
import com.unknown.guzhenren.item.gu.mortal.wisdom.CasualGuItem;
import com.unknown.guzhenren.item.gu.mortal.wisdom.MaliciousThoughtGuItem;
import com.unknown.guzhenren.item.gu.mortal.zombie.ZombieGuItem;
import com.unknown.guzhenren.item.material.LiquorItem;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.item.material.qi.DeathQiItem;
import com.unknown.guzhenren.item.material.qi.LifeQiItem;
import com.unknown.guzhenren.item.material.qi.QiMaterialItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item, and the only place a Gu's numbers actually live.
 *
 * <p>⚠ This chain is the truth. A figure written down anywhere else is a copy of it, and when the two
 * disagree, this file is the one that is right.
 *
 * @author Alex
 * @since 1.0.0
 */
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
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.TWO, GuPath.HEAVEN).refine(12)));
    public static final DeferredItem<Item> SILVER_RELICS_GU = ITEMS.register("silver_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.THREE, GuPath.HEAVEN).refine(120)));
    public static final DeferredItem<Item> GOLD_RELICS_GU = ITEMS.register("gold_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.FOUR, GuPath.HEAVEN).refine(1_200)));
    public static final DeferredItem<Item> CRYSTAL_RELICS_GU = ITEMS.register("crystal_relics_gu",
            () -> new RelicsGuItem(oneShot(), GuSpec.of(Rank.FIVE, GuPath.HEAVEN).refine(12_000)));
    //endregion

    //region 兽力虚影流 -- one round of 3,600 buys a beast's strength, held once ever
    public static final DeferredItem<Item> WHITE_BOAR_GU = ITEMS.register("white_boar_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.WHITE_BOAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_200)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.WHITE_BOAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BOAR_FEED, 1)));
    public static final DeferredItem<Item> BLACK_BOAR_GU = ITEMS.register("black_boar_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.BLACK_BOAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_200)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.BLACK_BOAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BOAR_FEED, 1)));
    public static final DeferredItem<Item> BEAR_STRENGTH_GU = ITEMS.register("bear_strength_gu",
            () -> new BeastStrengthGuItem(tended(), BeastStrength.BEAR, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_200)
                    .channel(3_600)
                    .speckEvery(600, BeastStrength.BEAR.getMarkTag())
                    .hungerBar(18, 3).hungerEvery(100)
                    .feed(ModItemTags.BEAR_FEED, 1)));
    //endregion

    //region 一转力道即时增益 -- one class, the effect and its length given at registration
    public static final DeferredItem<Item> FLOWER_BOAR_GU = ITEMS.register("flower_boar_gu",
            () -> new BuffGuItem(tended(), ModEffects.FLOWER_BOAR_GU, FlowerBoarGuEffect.DURATION_TICKS,
                    GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                            .refine(1_200)
                            .costPerUse(20)
                            .hungerBar(9, 3).hungerPerUse(3)
                            .feed(ModItemTags.BOAR_FEED, 1)
                            .cooldown(2 * Ticks.MINUTE)));
    public static final DeferredItem<Item> DRAGONPILL_CRICKET_GU = ITEMS.register("dragonpill_cricket_gu",
            () -> new BuffGuItem(tended(), ModEffects.DRAGONPILL_CRICKET_GU,
                    DragonpillCricketGuEffect.DURATION_TICKS,
                    GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                            .refine(1_200)
                            .costPerUse(20)
                            .hungerBar(8, 3).hungerPerUse(1)
                            .feed(ModItemTags.RABBIT_FEED, 1)
                            .cooldown(Ticks.MINUTE)));
    public static final DeferredItem<Item> BRUTE_FORCE_LONGHORN_BEETLE_GU =
            ITEMS.register("brute_force_longhorn_beetle_gu",
                    () -> new BuffGuItem(tended(), ModEffects.BRUTE_FORCE_LONGHORN_BEETLE_GU,
                            BruteForceLonghornBeetleGuEffect.DURATION_TICKS,
                            GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                                    .refine(1_200)
                                    .costPerUse(20)
                                    .hungerBar(8, 3).hungerPerUse(1)
                                    .feed(ModItemTags.BEEF_FEED, 1)
                                    .cooldown(Ticks.MINUTE)));
    //endregion

    //region 人力钧力流 -- one class, the kind at registration; one round is one layer
    public static final DeferredItem<Item> JIN_STRENGTH_GU = ITEMS.register("jin_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.JIN, GuSpec.of(Rank.ONE, GuPath.STRENGTH)
                    .refine(1_200)
                    .channel(3_600)
                    .speckEvery(3_600, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 3).hungerEvery(200)
                    .feed(ModItemTags.JIN_FEED, 1)
                    .dense(ModItemTags.JIN_FEED_DENSE, 9)));
    public static final DeferredItem<Item> TENS_JIN_STRENGTH_GU = ITEMS.register("tens_jin_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.TEN_JIN, GuSpec.of(Rank.TWO, GuPath.STRENGTH)
                    .refine(12_000)
                    .channel(36_000)
                    .speckEvery(3_600, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 9).hungerEvery(2_000)
                    .feed(ModItemTags.JIN_FEED, 1)
                    .dense(ModItemTags.JIN_FEED_DENSE, 9)));
    public static final DeferredItem<Item> JUN_STRENGTH_GU = ITEMS.register("jun_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.JUN, GuSpec.of(Rank.THREE, GuPath.STRENGTH)
                    .refine(120_000)
                    .channel(72_000)
                    .speckEvery(2_400, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 9).hungerEvery(4_000)
                    .feed(ModItemTags.JIN_FEED_SMELTED, 1)
                    .dense(ModItemTags.JIN_FEED_SMELTED_DENSE, 9)));
    public static final DeferredItem<Item> TENS_JUN_STRENGTH_GU = ITEMS.register("tens_jun_strength_gu",
            () -> new HumanStrengthGuItem(tended(), HumanStrength.TEN_JUN, GuSpec.of(Rank.FOUR, GuPath.STRENGTH)
                    .refine(1_200_000)
                    .channel(720_000)
                    .speckEvery(2_400, MarkTag.STRENGTH_HUMAN)
                    .hungerBar(36, 27).hungerEvery(40_000)
                    .feed(ModItemTags.JIN_FEED_SMELTED, 1)
                    .dense(ModItemTags.JIN_FEED_SMELTED_DENSE, 9)));
    //endregion

    //region 全力以赴蛊 -- 上古力道; the only thing that unlocks a stockpiled 9999 斤
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_3 = ITEMS.register("all_out_effort_gu_3",
            () -> new AllOutEffortGuItem(tended(), 60, GuSpec.of(Rank.THREE, GuPath.STRENGTH)
                    .refine(120_000)
                    .costPerUse(2_000)
                    .hungerBar(20, 8).hungerPerUse(5)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(80 * Ticks.SECOND)));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_4 = ITEMS.register("all_out_effort_gu_4",
            () -> new AllOutEffortGuItem(tended(), 90, GuSpec.of(Rank.FOUR, GuPath.STRENGTH)
                    .refine(1_200_000)
                    .costPerUse(20_000)
                    .hungerBar(20, 12).hungerPerUse(4)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(100 * Ticks.SECOND)));
    public static final DeferredItem<Item> ALL_OUT_EFFORT_GU_5 = ITEMS.register("all_out_effort_gu_5",
            () -> new AllOutEffortGuItem(tended(), 120, GuSpec.of(Rank.FIVE, GuPath.STRENGTH)
                    .refine(12_000_000)
                    .costPerUse(200_000)
                    .hungerBar(20, 16).hungerPerUse(2)
                    .feed(ModItemTags.ALL_OUT_FEED, 5)
                    .cooldown(120 * Ticks.SECOND)));
    //endregion

    //region Liquor Worm [酒虫] -- hunger bar 8 with 6 per use, and only its own rank can drive it
    public static final DeferredItem<Item> LIQUOR_WORM = ITEMS.register("liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.ONE, GuPath.FOOD)
                    .refine(1_200).costPerUse(20)
                    .hungerBar(8, 2).hungerPerUse(6).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> FOUR_FLAVORS_LIQUOR_WORM = ITEMS.register("four_flavors_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.TWO, GuPath.FOOD)
                    .refine(12_000).costPerUse(200)
                    .hungerBar(8, 4).hungerPerUse(6).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> SEVEN_FRAGRANCES_LIQUOR_WORM = ITEMS.register(
            "seven_fragrances_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.THREE, GuPath.FOOD)
                    .refine(120_000).costPerUse(2_000)
                    .hungerBar(8, 6).hungerPerUse(6).feed(ModItemTags.LIQUOR_FEED, 1)));
    public static final DeferredItem<Item> NINE_EYES_LIQUOR_WORM = ITEMS.register("nine_eyes_liquor_worm",
            () -> new LiquorWormItem(tended(), GuSpec.of(Rank.FOUR, GuPath.FOOD)
                    .refine(1_200_000).costPerUse(20_000)
                    .hungerBar(8, 8).hungerPerUse(6).feed(ModItemTags.LIQUOR_FEED, 1)));
    //endregion

    //region 元老蛊 -- a vault for 元石 that never needs feeding at all
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_1 = ITEMS.register("primeval_elder_gu_1",
            () -> new PrimevalElderGuItem(tended(), 1_000L, GuSpec.of(Rank.ONE, GuPath.SPACE)
                    .refine(10).costPerUse(1)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_2 = ITEMS.register("primeval_elder_gu_2",
            () -> new PrimevalElderGuItem(tended(), 10_000L, GuSpec.of(Rank.TWO, GuPath.SPACE)
                    .refine(100).costPerUse(1)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_3 = ITEMS.register("primeval_elder_gu_3",
            () -> new PrimevalElderGuItem(tended(), 100_000L, GuSpec.of(Rank.THREE, GuPath.SPACE)
                    .refine(1_200).costPerUse(1)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_4 = ITEMS.register("primeval_elder_gu_4",
            () -> new PrimevalElderGuItem(tended(), 1_000_000L, GuSpec.of(Rank.FOUR, GuPath.SPACE)
                    .refine(12_000).costPerUse(1)));
    public static final DeferredItem<Item> PRIMEVAL_ELDER_GU_5 = ITEMS.register("primeval_elder_gu_5",
            () -> new PrimevalElderGuItem(tended(), 100_000_000L, GuSpec.of(Rank.FIVE, GuPath.SPACE)
                    .refine(120_000).costPerUse(1)));
    //endregion

    //region 僵尸蛊 [zombie Gu] -- 变化道; a timed 半生半僵, and a 5-minute window that makes it permanent
    public static final DeferredItem<Item> ROAMING_ZOMBIE_GU = ITEMS.register("roaming_zombie_gu",
            () -> new ZombieGuItem(tended(), 16, 2 * Ticks.MINUTE, GuSpec.of(Rank.TWO, GuPath.TRANSFORMATION)
                    .refine(12_000).costPerUse(200)
                    .hungerBar(4, 2).hungerPerUse(2).feed(ModItemTags.ZOMBIE_FEED, 1)
                    .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> HAIRY_ZOMBIE_GU = ITEMS.register("hairy_zombie_gu",
            () -> new ZombieGuItem(tended(), 16, 4 * Ticks.MINUTE, GuSpec.of(Rank.THREE, GuPath.TRANSFORMATION)
                    .refine(120_000).costPerUse(2_000)
                    .hungerBar(8, 4).hungerPerUse(2).feed(ModItemTags.ZOMBIE_FEED, 1)
                    .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> HOPPING_ZOMBIE_GU = ITEMS.register("hopping_zombie_gu",
            () -> new ZombieGuItem(tended(), 32, 6 * Ticks.MINUTE, GuSpec.of(Rank.FOUR, GuPath.TRANSFORMATION)
                    .refine(1_200_000).costPerUse(20_000)
                    .hungerBar(16, 6).hungerPerUse(2).feed(ModItemTags.ZOMBIE_FEED, 1)
                    .cooldown(Ticks.SECOND)));

    public static final DeferredItem<Item> HEAVENLY_DEMON_ZOMBIE_GU = ITEMS.register("heavenly_demon_zombie_gu",
            () -> fifthRankZombieGu());
    public static final DeferredItem<Item> NIGHTMARE_ZOMBIE_GU = ITEMS.register("nightmare_zombie_gu",
            () -> fifthRankZombieGu());
    public static final DeferredItem<Item> ASURA_ZOMBIE_GU = ITEMS.register("asura_zombie_gu",
            () -> fifthRankZombieGu());
    public static final DeferredItem<Item> EARTH_CHIEF_ZOMBIE_GU = ITEMS.register("earth_chief_zombie_gu",
            () -> fifthRankZombieGu());
    public static final DeferredItem<Item> PLAGUE_ZOMBIE_GU = ITEMS.register("plague_zombie_gu",
            () -> fifthRankZombieGu());
    public static final DeferredItem<Item> BLOOD_WIGHT_GU = ITEMS.register("blood_wight_gu",
            () -> fifthRankZombieGu());

    private static ZombieGuItem fifthRankZombieGu() {
        return new ZombieGuItem(tended(), 32, 8 * Ticks.MINUTE, GuSpec.of(Rank.FIVE, GuPath.TRANSFORMATION)
                .refine(12_000_000).costPerUse(200_000)
                .hungerBar(16, 8).hungerPerUse(2).feed(ModItemTags.ZOMBIE_FEED, 1)
                .cooldown(Ticks.SECOND));
    }
    //endregion

    //region 更蛊 [Watch Gu] -- 宙道; tended like any other, and taken by the one use it is kept for
    public static final DeferredItem<Item> SECOND_WATCH_GU = ITEMS.register("second_watch_gu",
            () -> new WatchGuItem(tended(), ModEffects.SECOND_WATCH_GU, 5 * Ticks.MINUTE,
                    GuSpec.of(Rank.FOUR, GuPath.TIME)
                            .refine(1_600_000).costPerUse(20_000)
                            .hungerBar(12, 1).hungerPerUse(0).regainEvery(2_000)
                            .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> THIRD_WATCH_GU = ITEMS.register("third_watch_gu",
            () -> new WatchGuItem(tended(), ModEffects.THIRD_WATCH_GU, 5 * Ticks.MINUTE,
                    GuSpec.of(Rank.FIVE, GuPath.TIME)
                            .refine(16_000_000).costPerUse(200_000)
                             .hungerBar(12, 1).hungerPerUse(0).regainEvery(1_000)
                             .cooldown(Ticks.SECOND)));
    //endregion

    //region 恶念蛊 [Malicious Thought Gu] -- 智道; a one-use flood of evil thoughts, taken by its use
    public static final DeferredItem<Item> MALICIOUS_THOUGHT_GU_2 = ITEMS.register("malicious_thought_gu_2",
            () -> new MaliciousThoughtGuItem(tended(), ModEffects.MALICIOUS_THOUGHT_GU, 64L,
                    GuSpec.of(Rank.TWO, GuPath.WISDOM)
                            .refine(16_000).costPerUse(200)
                            .hungerBar(12, 4).hungerPerUse(0).feed(ModItemTags.MALICIOUS_THOUGHT_FEED, 1)
                            .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> MALICIOUS_THOUGHT_GU_3 = ITEMS.register("malicious_thought_gu_3",
            () -> new MaliciousThoughtGuItem(tended(), ModEffects.MALICIOUS_THOUGHT_GU, 640L,
                    GuSpec.of(Rank.THREE, GuPath.WISDOM)
                            .refine(160_000).costPerUse(2_000)
                            .hungerBar(12, 4).hungerPerUse(0).feed(ModItemTags.MALICIOUS_THOUGHT_FEED, 1)
                            .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> MALICIOUS_THOUGHT_GU_4 = ITEMS.register("malicious_thought_gu_4",
            () -> new MaliciousThoughtGuItem(tended(), ModEffects.MALICIOUS_THOUGHT_GU, 6_400L,
                    GuSpec.of(Rank.FOUR, GuPath.WISDOM)
                            .refine(1_600_000).costPerUse(20_000)
                            .hungerBar(12, 4).hungerPerUse(0).feed(ModItemTags.MALICIOUS_THOUGHT_FEED, 1)
                            .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> MALICIOUS_THOUGHT_GU_5 = ITEMS.register("malicious_thought_gu_5",
            () -> new MaliciousThoughtGuItem(tended(), ModEffects.MALICIOUS_THOUGHT_GU, 64_000L,
                    GuSpec.of(Rank.FIVE, GuPath.WISDOM)
                            .refine(16_000_000).costPerUse(200_000)
                            .hungerBar(12, 4).hungerPerUse(0).feed(ModItemTags.MALICIOUS_THOUGHT_FEED, 1)
                             .cooldown(Ticks.SECOND)));
    //endregion

    //region 胆识蛊 [Guts Gu] -- 魂道; a one-shot Gu that raises the soul cap
    public static final DeferredItem<Item> GUTS_GU = ITEMS.register("guts_gu",
            () -> new GutsGuItem(oneShot(), GuSpec.of(Rank.ONE, GuPath.SOUL)));
    //endregion

    //region 随意蛊 [Casual Gu] -- 智道; ten seconds of random thoughts, taken by its use
    public static final DeferredItem<Item> CASUAL_GU_1 = ITEMS.register("casual_gu_1",
            () -> new CasualGuItem(tended(), ModEffects.CASUAL_GU, GuSpec.of(Rank.ONE, GuPath.WISDOM)
                    .refine(1_600)
                    .costPerUse(20)
                    .hungerBar(9, 3).hungerPerUse(3)
                    .feed(ModItemTags.CASUAL_FEED, 1)
                    .cooldown(Ticks.SECOND)));
    public static final DeferredItem<Item> CASUAL_GU_2 = ITEMS.register("casual_gu_2",
            () -> new CasualGuItem(tended(), ModEffects.CASUAL_GU, GuSpec.of(Rank.TWO, GuPath.WISDOM)
                    .refine(16_000)
                    .costPerUse(200)
                    .hungerBar(9, 3).hungerPerUse(3)
                    .feed(ModItemTags.CASUAL_FEED, 1)
                    .cooldown(Ticks.SECOND)));
    //endregion

    //region 蛊材 [Gu materials]
    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
            () -> new PrimevalStoneItem(new Item.Properties(), PRIMEVAL_STONE_ESSENCE));

    public static final DeferredItem<Item> LIQUOR = ITEMS.register("liquor",
            () -> new LiquorItem(new Item.Properties()));

    public static final DeferredItem<Item> SOUR_LIQUOR = ITEMS.register("sour_liquor",
            () -> new LiquorItem(new Item.Properties()));

    public static final DeferredItem<Item> SWEET_LIQUOR = ITEMS.register("sweet_liquor",
            () -> new LiquorItem(new Item.Properties()));

    public static final DeferredItem<Item> BITTER_LIQUOR = ITEMS.register("bitter_liquor",
            () -> new LiquorItem(new Item.Properties()));

    public static final DeferredItem<Item> SPICY_LIQUOR = ITEMS.register("spicy_liquor",
            () -> new LiquorItem(new Item.Properties()));
    //endregion

    //region Qi Path [气道] materials -- 21, ranks I..V
    public static final DeferredItem<Item> SWORD_QI_1 = qiMaterial("sword_qi_1", Rank.ONE, QiKind.SWORD);
    public static final DeferredItem<Item> SWORD_QI_2 = qiMaterial("sword_qi_2", Rank.TWO, QiKind.SWORD);
    public static final DeferredItem<Item> SWORD_QI_3 = qiMaterial("sword_qi_3", Rank.THREE, QiKind.SWORD);
    public static final DeferredItem<Item> SWORD_QI_4 = qiMaterial("sword_qi_4", Rank.FOUR, QiKind.SWORD);
    public static final DeferredItem<Item> SWORD_QI_5 = qiMaterial("sword_qi_5", Rank.FIVE, QiKind.SWORD);

    public static final DeferredItem<Item> STRENGTH_QI_1 = qiMaterial("strength_qi_1", Rank.ONE, QiKind.STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_2 = qiMaterial("strength_qi_2", Rank.TWO, QiKind.STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_3 = qiMaterial("strength_qi_3", Rank.THREE, QiKind.STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_4 = qiMaterial("strength_qi_4", Rank.FOUR, QiKind.STRENGTH);
    public static final DeferredItem<Item> STRENGTH_QI_5 = qiMaterial("strength_qi_5", Rank.FIVE, QiKind.STRENGTH);

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

    public static final DeferredItem<Item> ESSENCE_QI_1 = qiMaterial("essence_qi_1", Rank.ONE, QiKind.ESSENCE);
    public static final DeferredItem<Item> ESSENCE_QI_2 = qiMaterial("essence_qi_2", Rank.TWO, QiKind.ESSENCE);
    public static final DeferredItem<Item> ESSENCE_QI_3 = qiMaterial("essence_qi_3", Rank.THREE, QiKind.ESSENCE);
    public static final DeferredItem<Item> ESSENCE_QI_4 = qiMaterial("essence_qi_4", Rank.FOUR, QiKind.ESSENCE);
    public static final DeferredItem<Item> ESSENCE_QI_5 = qiMaterial("essence_qi_5", Rank.FIVE, QiKind.ESSENCE);

    public static final DeferredItem<Item> DEATH_QI_5 = ITEMS.register("death_qi_5",
            () -> new DeathQiItem(qiProperties(), Rank.FIVE));

    private static DeferredItem<Item> qiMaterial(String id, Rank rank, QiKind kind) {
        return ITEMS.register(id, () -> new QiMaterialItem(qiProperties(), rank, kind));
    }

    private static Item.Properties qiProperties() {return new Item.Properties().stacksTo(64);}
    //endregion

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

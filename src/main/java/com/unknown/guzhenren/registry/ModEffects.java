package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.effect.AllOutEffortEffect;
import com.unknown.guzhenren.effect.BruteForceLonghornBeetleGuEffect;
import com.unknown.guzhenren.effect.DeathQiEffect;
import com.unknown.guzhenren.effect.DragonpillCricketGuEffect;
import com.unknown.guzhenren.effect.EssenceQiEffect;
import com.unknown.guzhenren.effect.FlowerBoarGuEffect;
import com.unknown.guzhenren.effect.LifeQiEffect;
import com.unknown.guzhenren.effect.LiquorWormEffect;
import com.unknown.guzhenren.effect.StrengthQiEffect;
import com.unknown.guzhenren.effect.VitalityLeafEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {

    private ModEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Guzhenren.MOD_ID);

    private static final int VITALITY_LEAF_COLOR                  = 0x4CAF50;
    private static final int LIQUOR_WORM_COLOR                    = 0x1565C0;
    private static final int LIFE_QI_COLOR                        = 0x4CAF50;
    private static final int ESSENCE_QI_COLOR                     = 0x4FC3F7;
    private static final int DEATH_QI_COLOR                       = 0x800000;
    private static final int FLOWER_BOAR_GU_COLOR                 = 0xF06292;
    private static final int ALL_OUT_EFFORT_COLOR                 = 0xFF8A65;
    private static final int DRAGONPILL_CRICKET_GU_COLOR          = 0xCDDC39;
    private static final int BRUTE_FORCE_LONGHORN_BEETLE_GU_COLOR = 0x455A64;
    private static final int STRENGTH_QI_COLOR                    = 0xFF7043;

    public static final DeferredHolder<MobEffect, VitalityLeafEffect> VITALITY_LEAF = MOB_EFFECTS.register(
            "vitality_leaf", () -> new VitalityLeafEffect(MobEffectCategory.BENEFICIAL, VITALITY_LEAF_COLOR));

    public static final DeferredHolder<MobEffect, LiquorWormEffect> LIQUOR_WORM = MOB_EFFECTS.register(
            "liquor_worm", () -> new LiquorWormEffect(MobEffectCategory.BENEFICIAL, LIQUOR_WORM_COLOR));

    public static final DeferredHolder<MobEffect, LifeQiEffect> LIFE_QI = MOB_EFFECTS.register(
            "life_qi", () -> new LifeQiEffect(MobEffectCategory.BENEFICIAL, LIFE_QI_COLOR));

    public static final DeferredHolder<MobEffect, EssenceQiEffect> ESSENCE_QI = MOB_EFFECTS.register(
            "essence_qi", () -> new EssenceQiEffect(MobEffectCategory.BENEFICIAL, ESSENCE_QI_COLOR));

    public static final DeferredHolder<MobEffect, DeathQiEffect> DEATH_QI = MOB_EFFECTS.register(
            "death_qi", () -> new DeathQiEffect(MobEffectCategory.HARMFUL, DEATH_QI_COLOR));

    public static final DeferredHolder<MobEffect, StrengthQiEffect> STRENGTH_QI = MOB_EFFECTS.register(
            "strength_qi", () -> new StrengthQiEffect(MobEffectCategory.BENEFICIAL, STRENGTH_QI_COLOR));

    public static final DeferredHolder<MobEffect, FlowerBoarGuEffect> FLOWER_BOAR_GU = MOB_EFFECTS.register(
            "flower_boar_gu", () -> new FlowerBoarGuEffect(MobEffectCategory.BENEFICIAL, FLOWER_BOAR_GU_COLOR));

    public static final DeferredHolder<MobEffect, AllOutEffortEffect> ALL_OUT_EFFORT = MOB_EFFECTS.register(
            "all_out_effort", () -> new AllOutEffortEffect(MobEffectCategory.BENEFICIAL, ALL_OUT_EFFORT_COLOR));

    public static final DeferredHolder<MobEffect, DragonpillCricketGuEffect> DRAGONPILL_CRICKET_GU =
            MOB_EFFECTS.register("dragonpill_cricket_gu", () -> new DragonpillCricketGuEffect(
                    MobEffectCategory.BENEFICIAL, DRAGONPILL_CRICKET_GU_COLOR));

    public static final DeferredHolder<MobEffect, BruteForceLonghornBeetleGuEffect> BRUTE_FORCE_LONGHORN_BEETLE_GU =
            MOB_EFFECTS.register("brute_force_longhorn_beetle_gu", () -> new BruteForceLonghornBeetleGuEffect(
                    MobEffectCategory.BENEFICIAL, BRUTE_FORCE_LONGHORN_BEETLE_GU_COLOR));

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}

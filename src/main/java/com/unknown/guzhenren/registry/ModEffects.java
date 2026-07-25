package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.effect.DeathQiEffect;
import com.unknown.guzhenren.effect.EssenceQiEffect;
import com.unknown.guzhenren.effect.LifeQiEffect;
import com.unknown.guzhenren.effect.LiquorWormEffect;
import com.unknown.guzhenren.effect.VitalityLeafEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Every custom MobEffect: what a Vitality Leaf Gu and the three qi materials leave behind, plus the
//  Liquor Worm [酒虫]. ⚠ A marker effect does nothing here -- its service reads it. See EssenceQiEffect.
public final class ModEffects {

    private ModEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Guzhenren.MOD_ID);

    //  Leaf green, for the particles the effect trails.
    private static final int VITALITY_COLOR = 0x4CAF50;

    //  ⚠ The HUD's distilled blue, not a liquor amber -- the effect and the bar it fills name the same
    //  thing, and two hues for one fact is exactly how a palette starts drifting.
    private static final int LIQUOR_COLOR = 0x1565C0;

    //  ⚠ Life Qi borrows the Vitality green on purpose -- both mean "health is coming back", and one
    //  fact wearing two hues is how a palette drifts. Essence Qi takes the HUD's essence blue for the
    //  same reason. Death Qi is the only new hue: a drained grey-purple, and nothing else uses it.
    private static final int LIFE_QI_COLOR = 0x4CAF50;
    private static final int ESSENCE_QI_COLOR = 0x4FC3F7;
    private static final int DEATH_QI_COLOR = 0x4A3A52;

    public static final DeferredHolder<MobEffect, VitalityLeafEffect> VITALITY_LEAF = MOB_EFFECTS.register(
            "vitality_leaf", () -> new VitalityLeafEffect(MobEffectCategory.BENEFICIAL, VITALITY_COLOR));

    public static final DeferredHolder<MobEffect, LiquorWormEffect> LIQUOR_WORM = MOB_EFFECTS.register(
            "liquor_worm", () -> new LiquorWormEffect(MobEffectCategory.BENEFICIAL, LIQUOR_COLOR));

    //  The three qi materials that leave something behind. ⚠ Essence Qi and Death Qi are pure markers:
    //  EssenceService.regenStep and PlayerTickEvents do their work -- see those two classes.
    public static final DeferredHolder<MobEffect, LifeQiEffect> LIFE_QI = MOB_EFFECTS.register(
            "life_qi", () -> new LifeQiEffect(MobEffectCategory.BENEFICIAL, LIFE_QI_COLOR));

    public static final DeferredHolder<MobEffect, EssenceQiEffect> ESSENCE_QI = MOB_EFFECTS.register(
            "essence_qi", () -> new EssenceQiEffect(MobEffectCategory.BENEFICIAL, ESSENCE_QI_COLOR));

    public static final DeferredHolder<MobEffect, DeathQiEffect> DEATH_QI = MOB_EFFECTS.register(
            "death_qi", () -> new DeathQiEffect(MobEffectCategory.HARMFUL, DEATH_QI_COLOR));

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}

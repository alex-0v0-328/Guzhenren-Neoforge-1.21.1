package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.effect.LiquorWormEffect;
import com.unknown.guzhenren.effect.VitalityLeafEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Every custom MobEffect: what a Vitality Leaf Gu leaves behind, and what a Liquor Worm [酒虫] does.
public final class ModEffects {

    private ModEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Guzhenren.MOD_ID);

    //  Leaf green, for the particles the effect trails.
    private static final int VITALITY_COLOR = 0x4CAF50;

    //  ⚠ The HUD's distilled blue, not a liquor amber -- the effect and the bar it fills name the same
    //  thing, and two hues for one fact is exactly how a palette starts drifting.
    private static final int LIQUOR_COLOR = 0x1565C0;

    public static final DeferredHolder<MobEffect, VitalityLeafEffect> VITALITY_LEAF = MOB_EFFECTS.register(
            "vitality_leaf", () -> new VitalityLeafEffect(MobEffectCategory.BENEFICIAL, VITALITY_COLOR));

    public static final DeferredHolder<MobEffect, LiquorWormEffect> LIQUOR_WORM = MOB_EFFECTS.register(
            "liquor_worm", () -> new LiquorWormEffect(MobEffectCategory.BENEFICIAL, LIQUOR_COLOR));

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}

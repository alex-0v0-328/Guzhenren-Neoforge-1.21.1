package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EssenceQiEffect extends MobEffect {

    private static final double[] REGEN_BONUS = {0.10, 0.15, 0.20, 0.30, 0.50};

    public EssenceQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static double bonus(int amplifier) {
        return REGEN_BONUS[Math.clamp(amplifier, 0, REGEN_BONUS.length - 1)];
    }
}

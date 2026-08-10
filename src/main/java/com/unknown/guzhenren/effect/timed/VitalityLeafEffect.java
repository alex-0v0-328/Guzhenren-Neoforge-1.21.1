package com.unknown.guzhenren.effect.timed;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VitalityLeafEffect extends MobEffect {

    public static final int DURATION_TICKS = 640;
    public static final int HEAL_INTERVAL_TICKS = 10;

    public VitalityLeafEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % HEAL_INTERVAL_TICKS == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.heal(1.0F);
        return true;
    }
}

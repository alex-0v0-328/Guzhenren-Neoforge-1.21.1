package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class LifeQiEffect extends MobEffect {

    public static final int HEAL_INTERVAL_TICKS = 10;

    public LifeQiEffect(MobEffectCategory category, int color) {
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

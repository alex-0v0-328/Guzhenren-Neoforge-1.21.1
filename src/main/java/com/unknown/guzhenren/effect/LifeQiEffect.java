package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

//  Life Qi [生气]: one health every ten ticks, for as long as the rank bought.
//  ⚠ The rank sets the DURATION only -- the rate is flat, the same shape VitalityLeafEffect uses.
public class LifeQiEffect extends MobEffect {

    public static final int HEAL_INTERVAL_TICKS = 10;

    public LifeQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    //  duration counts down, so the interval lands the same either way you measure it.
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

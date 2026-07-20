package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

//  Vitality (生机): one health every ten ticks, for thirty-two seconds.
//  ⚠ The two constants ARE the design -- 640 / 10 = 64 health. Change one and the other stops meaning it.
public class VitalityEffect extends MobEffect {

    public static final int DURATION_TICKS = 640;
    public static final int HEAL_INTERVAL_TICKS = 10;

    public VitalityEffect(MobEffectCategory category, int color) {
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

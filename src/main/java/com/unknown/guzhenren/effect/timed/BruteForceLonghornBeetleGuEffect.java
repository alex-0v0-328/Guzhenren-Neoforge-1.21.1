package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.effect.AttackContributor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * The timed buff of the Brute Force Longhorn Beetle Gu [蛮力天牛蛊], which charges on the way out.
 *
 * <p>⚠ The aftermath is applied on the effect's last tick, because a MobEffect gets no expiry hook.
 * Changing how that last tick is recognized is how the penalty quietly stops happening.
 *
 * @author Alex
 * @since 1.0.0
 */
public class BruteForceLonghornBeetleGuEffect extends MobEffect implements AttackContributor {

    public static final int DURATION_TICKS = 30 * Ticks.SECOND;
    public static final int AFTERMATH_TICKS = 20 * Ticks.SECOND;

    public static final double ATTACK_BONUS = 8.0D;

    private static final int LAST_TICK = 1;

    public BruteForceLonghornBeetleGuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public double attackBonus(int amplifier) {return ATTACK_BONUS;}

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {return duration == LAST_TICK;}

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, AFTERMATH_TICKS));
        return true;
    }
}

package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.effect.AttackContributor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The timed buff of the Flower Boar Gu [花豕蛊]: plain added attack damage for sixty seconds, no
 * cost on the way out.
 *
 * <p>Timed effects own their truth on vanilla's {@link net.minecraft.world.effect.MobEffect} timer —
 * unlike pool effects, they are not rebuilt every heartbeat. The bonus goes through {@link
 * com.unknown.guzhenren.effect.AttackContributor} so the attack total stays one number.
 *
 * <p>A timed buff alters nothing permanently.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.effect.AttackContributor
 * @since 1.0.0
 */

public class FlowerBoarGuEffect extends MobEffect implements AttackContributor {

    public static final int DURATION_TICKS = 60 * Ticks.SECOND;
    public static final double ATTACK_BONUS = 5.0D;
    public FlowerBoarGuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public double attackBonus(int amplifier) {return ATTACK_BONUS;}
}

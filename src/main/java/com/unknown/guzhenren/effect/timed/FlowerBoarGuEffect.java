package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.effect.AttackContributor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The timed buff of the Flower Boar Gu [花豕蛊]: plain added attack damage, no cost on the way out.
 *
 * @author Alex
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

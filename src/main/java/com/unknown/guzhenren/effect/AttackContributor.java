package com.unknown.guzhenren.effect;

/**
 * The one seam anything adds attack damage through.
 *
 * <p>⚠⚠ The attack sum walks the active effects and asks each one, instead of listing the effects it
 * knows about. An AttributeModifier here would be a second source for a single number.
 *
 * @author Alex
 * @since 1.0.0
 */
public interface AttackContributor {

    double attackBonus(int amplifier);
}

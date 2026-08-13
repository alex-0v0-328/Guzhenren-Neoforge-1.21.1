package com.unknown.guzhenren.effect;

/**
 * The one seam anything hastens the player's own clock through, and books what that speed is worth.
 *
 * <p>⚠ Rates SUM across the active effects rather than taking the best one -- two Watch Gu are meant
 * to be worn together, and the sum is the number he asked to see.
 *
 * @author Alex
 * @since 1.0.0
 * @see AttackContributor
 */
public interface TimeFlowContributor {

    int timeRate(int amplifier);

    long timeSpecks(int amplifier);
}

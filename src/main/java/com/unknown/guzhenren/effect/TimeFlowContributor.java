package com.unknown.guzhenren.effect;

/**
 * The one seam anything hastens the player's own clock [自身时间] through, and books what that speed
 * is worth in specks [碎屑].
 *
 * <p>Implemented by {@link com.unknown.guzhenren.effect.timed.TimeFlowEffect}; {@link
 * com.unknown.guzhenren.attachment.service.body.TimeFlowService} walks {@code getActiveEffects()}
 * and sums every contributor's rate, so a new 宙道 effect needs no edit anywhere else.
 *
 * <p>⚠ Rates SUM and nothing refuses — two Watch Gu [更蛊] worn together add their rates and their
 * specks. The sum floors at 1, never {@code 1 + Σ(rate − 1)}. Only {@code InfoModel} may read the
 * rate to print it; doing arithmetic on it at a call site is how 寿元 once aged backwards.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.effect.timed.TimeFlowEffect
 * @see AttackContributor
 */
public interface TimeFlowContributor {

    int timeRate(int amplifier);

    long timeSpecks(int amplifier);
}

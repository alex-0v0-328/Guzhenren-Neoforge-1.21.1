package com.unknown.guzhenren.effect.pool;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Death Qi [死气], the pool effect that burns lifespan [寿元] for as long as it is held.
 *
 * <p>⚠ This class holds only the numbers; the burning runs on the heartbeat. A MobEffect gets no
 * expiry hook here, and the debt has to be settled at the moment the holding ends.
 *
 * @author Alex
 * @since 1.0.0
 */
public class DeathQiEffect extends MobEffect {

    public static final int YEAR_INTERVAL_TICKS = 120;
    public static final long YEARS_PER_INTERVAL = 1L;

    public static final float HEALTH_FLOOR = 2.0F;
    public static final float HEALTH_PER_HEARTBEAT = 1.0F;

    public static final int REFUND_NUMERATOR = 3;
    public static final int REFUND_DENOMINATOR = 4;

    public DeathQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}

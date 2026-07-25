package com.unknown.guzhenren.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

//  Death Qi [死气]: lifespan bleeds away, essence stops regenerating, health falls to one heart.
//  ⚠ A pure MARKER -- PlayerTickEvents.tickDeathQi does the work, because the tally of lifespan taken
//  must outlive the effect. Milk and /effect clear end it with no refund; only Life Qi [生气] hands any back.
public class DeathQiEffect extends MobEffect {

    //  Six seconds a year -- ten years a minute, and 120 ticks lands exactly on the per-second heartbeat,
    //  so nothing has to carry a remainder the way essence regen does.
    public static final int YEAR_INTERVAL_TICKS = 120;
    public static final long YEARS_PER_INTERVAL = 1L;

    //  Bleeding stops at one heart: Death Qi takes the lifespan, never the last of the health.
    //  ⚠ Per HEARTBEAT, not per tick -- PlayerTickEvents only wakes every EssenceService.REGEN_INTERVAL_TICKS,
    //  so an interval finer than that is unreachable. One health a second, floor at one heart.
    public static final float HEALTH_FLOOR = 2.0F;
    public static final float HEALTH_PER_HEARTBEAT = 1.0F;

    //  ⚠ Refund is three quarters, and only Life Qi pays it. Written here so the ratio has one home.
    public static final int REFUND_NUMERATOR = 3;
    public static final int REFUND_DENOMINATOR = 4;

    public DeathQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}

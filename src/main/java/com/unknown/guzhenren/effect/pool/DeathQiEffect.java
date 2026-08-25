package com.unknown.guzhenren.effect.pool;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Death Qi [死气] effect — a pool projection of the 死气 held in {@link
 * com.unknown.guzhenren.attachment.data.body.QiData}, which burns lifespan [寿元] and floors health
 * while the amount is above zero.
 *
 * <p>Pool effects are rebuilt every heartbeat by {@code QiService.syncEffects}, so milk cannot cure
 * them. This class holds only the constants; the actual burning runs on the heartbeat in {@code
 * PlayerTickEvents.tickDeathQi}, because a {@link net.minecraft.world.effect.MobEffect} has no
 * expiry hook and the lifespan debt must be settled by reading the level.
 *
 * <p>⚠ {@code YEAR_INTERVAL_TICKS} is 120 = 6 × 20 — it must divide the heartbeat's 20, or the
 * burning silently stops happening. 生气 [Life Qi] pays 死气 down 1:1; only clearing it refunds
 * {@code REFUND_NUMERATOR / REFUND_DENOMINATOR} of the burnt 寿元.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.QiService
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

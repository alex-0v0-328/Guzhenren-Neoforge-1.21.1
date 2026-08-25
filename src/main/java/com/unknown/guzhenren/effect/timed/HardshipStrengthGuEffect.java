package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.client.ItemEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Hardship Strength Gu [苦力蛊] effect: the timed carrier -- the bonus itself is read from the
 * holder's health fraction, not from here.
 *
 * <p>This class carries only the duration and the icon; the 0..120 capacity bonus is computed in
 * {@link com.unknown.guzhenren.attachment.service.body.StrengthService#capacity} off missing
 * health, so the effect staying present IS what keeps that ramp alive.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.StrengthService
 * @since 1.0.0
 */

public final class HardshipStrengthGuEffect extends MobEffect {

    public static final int DURATION_TICKS = 120 * Ticks.SECOND;

    public HardshipStrengthGuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon("hardship_strength_gu"));
    }
}

package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.GradedEffectIcon;
import com.unknown.guzhenren.effect.TimeFlowContributor;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * The 宙道 [Time Path] form: while it runs, the wearer's own clock [自身时间] runs faster than the world's.
 *
 * <p>⚠ Its own remaining duration is the world's time and is never hastened -- an effect that shortened
 * itself would end before the length it promised.
 *
 * @author Alex
 * @since 1.0.0
 * @see TimeFlowContributor
 */
public class TimeFlowEffect extends MobEffect implements TimeFlowContributor {

    /** Indexed by amplifier, which is the Gu's tier; the lower rungs carry no Time Path Gu yet. */
    private static final int[] RATE_BY_AMPLIFIER = {1, 1, 1, 2, 3};

    private static final int LOWEST_RANK = 4;
    private static final int HIGHEST_RANK = 5;

    public TimeFlowEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public int timeRate(int amplifier) {
        return RATE_BY_AMPLIFIER[Math.clamp(amplifier, 0, RATE_BY_AMPLIFIER.length - 1)];
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new GradedEffectIcon("time_flow", LOWEST_RANK, HIGHEST_RANK));
    }
}

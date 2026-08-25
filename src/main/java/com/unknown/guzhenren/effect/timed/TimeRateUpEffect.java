package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.ItemEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Time Rate Up [自身时间加速]: each layer makes the wearer's own clock outrun the world by its base rate.
 *
 * <p>One effect per Watch Gu [更蛊], so both kinds can run together and their capped rates add. The
 * effect duration remains world time: five minutes never hastens itself.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.TimeFlowService
 * @since 1.0.0
 */

public class TimeRateUpEffect extends MobEffect {

    private final int ratePerLayer;
    private final int maxLayers;
    private final String icon;

    public TimeRateUpEffect(MobEffectCategory category, int color, int ratePerLayer, int maxLayers, String icon) {
        super(category, color);
        this.ratePerLayer = ratePerLayer;
        this.maxLayers = maxLayers;
        this.icon = icon;
    }

    public int nextAmplifier(int amplifier) {return Math.clamp(amplifier + 1, 0, maxLayers - 1);}
    public int timeRate(int amplifier) {return ratePerLayer * Math.clamp(amplifier + 1, 1, maxLayers);}

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon(icon));
    }
}

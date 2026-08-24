package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.ItemEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * The 宙道 [Time Path] timed form: while it runs, the wearer's own clock [自身时间] outruns the
 * world's by the declared rate.
 *
 * <p>Timed effects own their truth on vanilla's timer. One effect per Gu rather than a graded
 * family, precisely so two Watch Gu [更蛊] can be worn together and their rates add. {@link
 * com.unknown.guzhenren.attachment.service.body.TimeFlowService} sums every time effect.
 *
 * <p>⚠ The effect's own remaining duration is WORLD time and is never hastened — five minutes is
 * five real minutes.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.TimeFlowService
 */
public class TimeFlowEffect extends MobEffect {

    private final int rate;
    private final String icon;

    public TimeFlowEffect(MobEffectCategory category, int color, int rate, String icon) {
        super(category, color);
        this.rate = rate;
        this.icon = icon;
    }

    public int timeRate(int amplifier) {return rate;}

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon(icon));
    }
}

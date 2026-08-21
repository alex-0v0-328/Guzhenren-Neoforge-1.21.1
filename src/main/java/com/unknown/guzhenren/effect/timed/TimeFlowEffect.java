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
 * com.unknown.guzhenren.attachment.service.body.TimeFlowService} sums every time effect and derives
 * the specks [碎屑] under {@code MarkTag.TIME_FLOW} every heartbeat.
 *
 * <p>⚠ The effect's own remaining duration is WORLD time and is never hastened — five minutes is
 * five real minutes. Specks are lent and taken back on expiry, milk, {@code /effect clear} or death;
 * the tag has no writer and is derived by {@code syncSpecks}.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.TimeFlowService
 */
public class TimeFlowEffect extends MobEffect {

    private final int rate;
    private final long specks;
    private final String icon;

    public TimeFlowEffect(MobEffectCategory category, int color, int rate, long specks, String icon) {
        super(category, color);
        this.rate = rate;
        this.specks = specks;
        this.icon = icon;
    }

    public int timeRate(int amplifier) {return rate;}

    public long timeSpecks(int amplifier) {return specks;}

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon(icon));
    }
}

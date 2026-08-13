package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.ItemEffectIcon;
import com.unknown.guzhenren.effect.TimeFlowContributor;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * The 宙道 [Time Path] form: while it runs, the wearer's own clock [自身时间] outruns the world's.
 *
 * <p>⚠ One of these per Gu rather than one graded family, precisely so two can be worn together and
 * their rates add. Its own remaining duration is the world's time and is never hastened.
 *
 * @author Alex
 * @since 1.0.0
 * @see TimeFlowContributor
 */
public class TimeFlowEffect extends MobEffect implements TimeFlowContributor {

    private final int rate;
    private final long specks;
    private final String icon;

    public TimeFlowEffect(MobEffectCategory category, int color, int rate, long specks, String icon) {
        super(category, color);
        this.rate = rate;
        this.specks = specks;
        this.icon = icon;
    }

    @Override
    public int timeRate(int amplifier) {return rate;}

    @Override
    public long timeSpecks(int amplifier) {return specks;}

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon(icon));
    }
}

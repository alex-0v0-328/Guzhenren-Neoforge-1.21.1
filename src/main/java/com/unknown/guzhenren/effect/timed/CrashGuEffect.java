package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.client.GradedEffectIcon;
import com.unknown.guzhenren.client.ItemEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * The Crash Gu family [横冲蛊 / 直撞蛊 / 横冲直撞蛊]: one class carrying all three dashes --
 * horizontal, vertical, and the charging one that moves on both axes.
 *
 * <p>The class holds only the axis flags and the shared duration helper; the movement itself is
 * reported by the client through {@link com.unknown.guzhenren.network.CrashStepPayload}. The
 * charging shape grades its icon by rank because it spans ranks four and five.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.network.CrashStepPayload
 * @since 1.0.0
 */

public final class CrashGuEffect extends MobEffect {

    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    private final int axes;

    public CrashGuEffect(MobEffectCategory category, int color, int axes) {
        super(category, color);
        this.axes = axes;
    }

    public static int duration(int seconds) {return seconds * Ticks.SECOND;}
    public boolean horizontal() {return (axes & HORIZONTAL) != 0;}
    public boolean vertical() {return (axes & VERTICAL) != 0;}

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        if (axes == HORIZONTAL) consumer.accept(new ItemEffectIcon("horizontal_crash_gu"));
        else if (axes == VERTICAL) consumer.accept(new ItemEffectIcon("vertical_crash_gu"));
        else consumer.accept(GradedEffectIcon.item("charging_crash_gu", 4, 5));
    }
}

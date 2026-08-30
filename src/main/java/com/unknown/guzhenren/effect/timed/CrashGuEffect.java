package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The Crash Gu family [横冲蛊 / 直撞蛊 / 横冲直撞蛊]: one class carrying all three dashes --
 * horizontal, vertical, and the charging one that moves on both axes.
 *
 * <p>The class holds only the axis flags and the shared duration helper; the movement itself is
 * reported by the client through {@link com.unknown.guzhenren.network.payload.DashPayload}. The
 * charging shape grades its icon by rank because it spans ranks four and five.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.network.payload.DashPayload
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
}

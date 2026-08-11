package com.unknown.guzhenren;

/**
 * Every unit of time this mod speaks, in ticks.
 *
 * <p>⚠ The mod has no config, so a duration someone might one day want to tune still belongs here as a
 * named constant rather than inline in whatever code happens to use it first.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class Ticks {

    private Ticks() {}

    public static final int SECOND = 20;
    public static final int HALF_SECOND = SECOND / 2;
    public static final int MINUTE = 60 * SECOND;

    public static final int DAY = 24000;
    public static final int HALF_DAY = DAY / 2;
}

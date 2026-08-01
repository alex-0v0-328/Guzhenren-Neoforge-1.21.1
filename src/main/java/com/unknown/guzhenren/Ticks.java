package com.unknown.guzhenren;

//  Minecraft's units of time, in ticks. ⚠⚠ CONSTANTS, never config: a server owner cannot make a day
//  something other than 24000, and letting one try would only desync aging, regen and feeding from the
//  actual day/night cycle. This mod ships no config at all, deliberately -- see GuzhenrenClient.
//  ⚠ Root package, beside the entry points: time belongs to no domain, and services, items and events
//  all read it. There is no util/ here on purpose, so a shared vocabulary lives where the mod does.
public final class Ticks {

    private Ticks() {}

    public static final int SECOND = 20;
    public static final int MINUTE = 60 * SECOND;

    //  The world clock. ⚠ Lifespan is counted in these, and EssenceService turns a per-day regen rate
    //  into a per-tick one with it -- one number, so the two can never drift apart.
    public static final int DAY = 24000;
    public static final int HALF_DAY = DAY / 2;
}

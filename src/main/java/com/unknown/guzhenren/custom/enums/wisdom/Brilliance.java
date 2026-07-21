package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Brilliance (才情) IS the regen rate of thoughts. Rolled by weight at BIRTH, never at awakening, and
//  fixed for life  CLAUDE.md "Mind Ocean".
//  ⚠ Unlike Talent, these run LOW to HIGH, so shift(+1) really is ordinal + 1.
public enum Brilliance implements StringRepresentable, EnumTranslatable {

    //  thoughtsPerSecond (thoughts a second, so per 20 ticks), weight (roll weight)
    ORDINARY(1, 15),
    DECENT(4, 25),
    DISTINCTIVE(16, 25),
    OUTSTANDING(64, 25),
    UNRIVALED(256, 10);

    public static final Codec<Brilliance> CODEC = StringRepresentable.fromEnum(Brilliance::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.brilliance.";

    //  ⚠ No NONE: a mortal still thinks. Ordinary Brilliance is the floor, not an absence.
    public static final Brilliance LOWEST = ORDINARY;
    public static final Brilliance HIGHEST = UNRIVALED;

    private final long thoughtsPerSecond;
    private final int weight;

    Brilliance(long thoughtsPerSecond, int weight) {
        this.thoughtsPerSecond = thoughtsPerSecond;
        this.weight = weight;
    }

    public long getThoughtsPerSecond() {return thoughtsPerSecond;}
    public int getWeight() {return weight;}

    //  Shift d grades, positive = better, stopping at the edge.
    public Brilliance shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  Weighted roll: 15 / 25 / 25 / 25 / 10.
    public static Brilliance randomBrilliance() {
        int total = 0;
        for (Brilliance b : values()) total += b.weight;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (Brilliance b : values()) {
            roll -= b.weight;
            if (roll < 0) return b;
        }
        return ORDINARY;
    }
}

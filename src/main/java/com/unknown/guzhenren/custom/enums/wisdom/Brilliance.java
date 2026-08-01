package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Brilliance implements StringRepresentable, EnumTranslatable {

    ORDINARY(1, 15),
    DECENT(4, 25),
    DISTINCTIVE(16, 25),
    OUTSTANDING(64, 25),
    UNRIVALED(256, 10);

    public static final Codec<Brilliance> CODEC = StringRepresentable.fromEnum(Brilliance::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.brilliance.";

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

    public Brilliance shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

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

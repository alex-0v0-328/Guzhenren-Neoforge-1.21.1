package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Talent implements StringRepresentable, EnumTranslatable {

    EXTREME(100, 100, 10, 20),
    FIRST(80, 99, 20, 8),
    SECOND(60, 79, 30, 4),
    THIRD(40, 59, 30, 2),
    FOURTH(20, 39, 10, 1),

    NONE(0, 0, 0, 0);

    public static final Codec<Talent> CODEC = StringRepresentable.fromEnum(Talent::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.talent.";

    public static final Talent HIGHEST = EXTREME;
    public static final Talent LOWEST = FOURTH;

    private final int minPercent;
    private final int maxPercent;
    private final int weight;
    private final int regenRate;

    Talent(int minPercent, int maxPercent, int weight, int regenRate) {
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
        this.weight = weight;
        this.regenRate = regenRate;
    }

    public int getMinPercent() {return minPercent;}
    public int getMaxPercent() {return maxPercent;}
    public int getWeight() {return weight;}
    public int getRegenRate() {return regenRate;}

    public Talent shift(int d) {return values()[Math.clamp(ordinal() - d, HIGHEST.ordinal(), LOWEST.ordinal())];}
    public static Talent[] settable() {return Arrays.copyOfRange(values(), HIGHEST.ordinal(), LOWEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    public static Talent randomTalent() {
        int total = 0;
        for (Talent t : values()) total += t.weight;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (Talent t : values()) {
            roll -= t.weight;
            if (roll < 0) return t;
        }
        return NONE;
    }

    public static Talent randomNormalTalent() {
        int total = 0;
        for (Talent t : values()) {
            if (t != EXTREME) total += t.weight;
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (Talent t : values()) {
            if (t == EXTREME) continue;
            roll -= t.weight;
            if (roll < 0) return t;
        }
        return NONE;
    }

    public static int randomPercent(Talent talent) {
        if (talent.minPercent == talent.maxPercent) return talent.minPercent;
        return ThreadLocalRandom.current().nextInt(talent.minPercent, talent.maxPercent + 1);
    }

    public static Talent fromPercent(int percent) {
        for (Talent t : values()) {
            if (percent >= t.minPercent && percent <= t.maxPercent) return t;
        }
        return NONE;
    }
}

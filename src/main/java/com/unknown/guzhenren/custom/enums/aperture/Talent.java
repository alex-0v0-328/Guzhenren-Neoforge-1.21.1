package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Talent implements StringRepresentable, EnumTranslatable {

    //  minPercent, maxPercent, weight (roll weight), regenRate (essence regen multiplier)
    //  regenRate scales a notional 100 a day, never his own aptitude base  EssenceService.regenPerDay
    EXTREME(100, 100, 10, 20),
    FIRST(80, 99, 20, 8),
    SECOND(60, 79, 30, 4),
    THIRD(40, 59, 30, 2),
    FOURTH(20, 39, 10, 1),

    //  Unawakened: a placeholder, never rolled. A live aptitude base is 20..100, so no ordinary player
    //  ever lands here.
    NONE(0, 0, 0, 0);

    public static final Codec<Talent> CODEC = StringRepresentable.fromEnum(Talent::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.talent.";

    //  ⚠ These run HIGH to LOW (Ten-Extremes > A > B > C > D), so one grade better is ordinal - 1 --
    //  sealed inside shift(), so callers pass +1 and never touch ordinal. NONE sits outside that range.
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

    //  Shift d grades, positive = better, stopping at the edge. ⚠ Note the minus -- see above.
    public Talent shift(int d) {return values()[Math.clamp(ordinal() - d, HIGHEST.ordinal(), LOWEST.ordinal())];}
    public static Talent[] settable() {return Arrays.copyOfRange(values(), HIGHEST.ordinal(), LOWEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  Weighted roll; NONE carries weight 0 and can never come up.
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

    //  The same roll without Ten-Extremes; what a revoked physique falls back to.
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

    //  Roll an aptitude base inside that grade's band.
    public static int randomPercent(Talent talent) {
        if (talent.minPercent == talent.maxPercent) return talent.minPercent;
        return ThreadLocalRandom.current().nextInt(talent.minPercent, talent.maxPercent + 1);
    }

    //  Which grade a base falls in; NONE when it matches none.
    public static Talent fromPercent(int percent) {
        for (Talent t : values()) {
            if (percent >= t.minPercent && percent <= t.maxPercent) return t;
        }
        return NONE;
    }
}

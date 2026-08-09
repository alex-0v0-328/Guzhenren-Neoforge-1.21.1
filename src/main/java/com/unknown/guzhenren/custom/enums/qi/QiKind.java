package com.unknown.guzhenren.custom.enums.qi;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum QiKind implements StringRepresentable, EnumTranslatable {

    SWORD    ( 5 * Ticks.MINUTE,                 0),
    STRENGTH (10 * Ticks.MINUTE,                 0),
    LIFE     (                0, 2 * Ticks.MINUTE),
    ESSENCE  (                0, 2 * Ticks.MINUTE),
    DEATH    (                0,                 0),
    HUMAN    (                0,                 0),
    HEAVEN   (                0,                 0),
    EARTH    (                0,                 0);

    public static final Codec<QiKind> CODEC = StringRepresentable.fromEnum(QiKind::values);
    private static final String KEY_PREFIX = "guzhenren.enum.qi.";

    private static final int TIERS = 5;

    private final long holdFlatTicks;
    private final long holdPerTierTicks;

    QiKind(long holdFlatTicks, long holdPerTierTicks) {
        this.holdFlatTicks = holdFlatTicks;
        this.holdPerTierTicks = holdPerTierTicks;
    }

    public boolean isTimed() {return holdFlatTicks > 0L || holdPerTierTicks > 0L;}

    public long holdTicks(int tier) {
        return holdFlatTicks > 0L ? holdFlatTicks : (tier + 1L) * holdPerTierTicks;
    }

    public static long tierAmount(int tier) {return 10L << (2 * tier);}
    public static long decayPerSecond(int tier) {return 1L << tier;}

    public static int tierOf(long amount) {
        for (int tier = TIERS - 1; tier >= 0; tier--) {
            if (amount >= tierAmount(tier)) return tier;
        }
        return -1;
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.soul;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Soul (魂魄) tier, looked up from the soul value and never stored. The number is the inclusive floor:
//  people x 100, except One-Person Soul, which is pulled down to 1.
//  ⚠ Hundred-Million-Person Soul is the top tier but not a ceiling: anything above still reads as it.
public enum SoulTier implements StringRepresentable, EnumTranslatable {

    ONE(1L),
    TEN(1_000L),
    HUNDRED(10_000L),
    THOUSAND(100_000L),
    TEN_THOUSAND(1_000_000L),
    HUNDRED_THOUSAND(10_000_000L),
    MILLION(100_000_000L),
    TEN_MILLION(1_000_000_000L),
    HUNDRED_MILLION(10_000_000_000L);

    public static final Codec<SoulTier> CODEC = StringRepresentable.fromEnum(SoulTier::values);
    private static final String KEY_PREFIX = "guzhenren.enum.soul.tier.";

    //  The inclusive soul value this tier begins at.
    private final long minSoul;

    SoulTier(long minSoul) {
        this.minSoul = minSoul;
    }

    public long getMinSoul() {return minSoul;}

    //  The highest tier whose floor he reaches. ⚠ A soul of 0 falls through to One-Person Soul -- by
    //  then he is already dead.
    public static @NotNull SoulTier fromSoul(long soul) {
        SoulTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (soul >= tiers[i].minSoul) return tiers[i];
        }
        return ONE;
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

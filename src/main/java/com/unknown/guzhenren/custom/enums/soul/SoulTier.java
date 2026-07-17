package com.unknown.guzhenren.custom.enums.soul;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  魂魄境界: 由魂魄值反查, 不单独存. 括号里是入档门槛(含), 规律为 人数 × 100, 一人魂的起点压到 1
//  亿人魂是顶档但不封顶: 再高也仍是亿人魂
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

    //  进入本境界的魂魄值门槛 (含)
    private final long minSoul;

    SoulTier(long minSoul) {
        this.minSoul = minSoul;
    }

    public long getMinSoul() {return minSoul;}

    //  从高往低找第一个够得着的门槛. 魂魄值 0 会兜底成一人魂, 但那一刻人已经死了
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

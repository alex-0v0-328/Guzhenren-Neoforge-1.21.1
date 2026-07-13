package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  魂魄境界: 由魂魄值反查, 不单独存 (同「资质基数 -> 资质档」的做法)
//  括号里是进入该境界的门槛(含), 规律为 人数 × 100 —— 一人魂的起点压到 1
//   一人魂 1~999 / 十人魂 1000~9999 / 百人魂 10000~99999 / ...
//  亿人魂是顶档但不是封顶: 魂魄值再高也仍是亿人魂
public enum GuSoulTier implements StringRepresentable {

    ONE(1L),
    TEN(1_000L),
    HUNDRED(10_000L),
    THOUSAND(100_000L),
    TEN_THOUSAND(1_000_000L),
    HUNDRED_THOUSAND(10_000_000L),
    MILLION(100_000_000L),
    TEN_MILLION(1_000_000_000L),
    HUNDRED_MILLION(10_000_000_000L);

    public static final Codec<GuSoulTier> CODEC = StringRepresentable.fromEnum(GuSoulTier::values);
    private static final String KEY_PREFIX = "guzhenren.enum.soul.tier.";

    //  进入本境界的魂魄值门槛 (含)
    private final long minSoul;

    GuSoulTier(long minSoul) {
        this.minSoul = minSoul;
    }

    public long getMinSoul() {return minSoul;}

    //  反查: 从高往低找第一个够得着的门槛; 顶档之上仍是亿人魂 (不封顶)
    //  魂魄值 0 会兜底成一人魂, 但那一刻玩家已经魂魄衰竭而亡了, 见 SoulService
    public static @NotNull GuSoulTier fromSoul(long soul) {
        GuSoulTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (soul >= tiers[i].minSoul) return tiers[i];
        }
        return ONE;
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

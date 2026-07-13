package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuTalent implements StringRepresentable {

    //  minPercent, maxPercent, weight(抽取权重: 甲20 乙30 丙30 丁10 十绝10), regenRate(真元回复倍率)
    //  regenRate 的基准恒定按 baseEssence=100 算, 与玩家自己的资质基数无关, 详见 EssenceService.regenPerDay
    //  即: 一转初阶下, 丁等一天(24000 tick)回 100 点, 十绝一天回 2000 点
    EXTREME(100, 100, 10, 20),
    FIRST(80, 99, 20, 8),
    SECOND(60, 79, 30, 4),
    THIRD(40, 59, 30, 2),
    FOURTH(20, 39, 10, 1),

    //  未觉醒: 只是"尚未开窍"的占位, 不参与抽取, 也不回复真元
    //  资质基数的合法区间是 20~100 (见 CoreData), 所以正常玩家绝不会落到这一档
    NONE(0, 0, 0, 0);

    public static final Codec<GuTalent> CODEC = StringRepresentable.fromEnum(GuTalent::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.talent.";

    private final int minPercent;
    private final int maxPercent;
    private final int weight;
    private final int regenRate;

    GuTalent(int minPercent, int maxPercent, int weight, int regenRate) {
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
        this.weight = weight;
        this.regenRate = regenRate;
    }

    public int getMinPercent() {return minPercent;}
    public int getMaxPercent() {return maxPercent;}
    public int getWeight() {return weight;}
    public int getRegenRate() {return regenRate;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  按权重随机一个资质档 (甲20 乙30 丙30 丁10 十绝10); 权重为 0 的 NONE 不会被抽中
    public static GuTalent randomTalent() {
        int total = 0;
        for (GuTalent t : values()) total += t.weight;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (GuTalent t : values()) {
            roll -= t.weight;
            if (roll < 0) return t;
        }
        return NONE;
    }

    //  按权重随机一个普通资质档 (甲乙丙丁), 排除十绝与 NONE; 用于取消十绝体质时回落
    public static GuTalent randomNormalTalent() {
        int total = 0;
        for (GuTalent t : values()) {
            if (t != EXTREME) total += t.weight;
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (GuTalent t : values()) {
            if (t == EXTREME) continue;
            roll -= t.weight;
            if (roll < 0) return t;
        }
        return NONE;
    }

    //  在 talent 区间内 roll 一个百分比 (0-100)
    public static int randomPercent(GuTalent talent) {
        if (talent.minPercent == talent.maxPercent) return talent.minPercent;
        return ThreadLocalRandom.current().nextInt(talent.minPercent, talent.maxPercent + 1);
    }

    //  根据百分比反查所属 talent tier. 无匹配时返回 NONE
    public static GuTalent fromPercent(int percent) {
        for (GuTalent t : values()) {
            if (percent >= t.minPercent && percent <= t.maxPercent) return t;
        }
        return NONE;
    }
}

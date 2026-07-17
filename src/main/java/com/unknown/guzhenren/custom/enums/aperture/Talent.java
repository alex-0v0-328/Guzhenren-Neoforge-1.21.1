package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Talent implements StringRepresentable, EnumTranslatable {

    //  minPercent, maxPercent, weight(抽取权重), regenRate(真元回复倍率)
    //  regenRate 的基准恒定按 100 算, 与玩家自己的资质基数无关 —— 见 EssenceService.regenPerDay
    EXTREME(100, 100, 10, 20),
    FIRST(80, 99, 20, 8),
    SECOND(60, 79, 30, 4),
    THIRD(40, 59, 30, 2),
    FOURTH(20, 39, 10, 1),

    //  未觉醒: 「尚未开窍」的占位, 不参与抽取. 资质基数合法区间是 20~100, 正常玩家落不到这一档
    NONE(0, 0, 0, 0);

    public static final Codec<Talent> CODEC = StringRepresentable.fromEnum(Talent::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.talent.";

    //  ⚠ 本枚举从高到低排 (十绝 > 甲 > 乙 > 丙 > 丁), 所以升一档是 ordinal - 1. 方向只封在 shift() 里,
    //  调用方一律 shift(+1) = 升一档, 不要自己动 ordinal. NONE 在区间外 —— 那是 awaken / reset 的事
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

    //  升 d 档 (正数 = 更好), 到边界即停. 注意减号 —— 见上面的 ⚠
    public Talent shift(int d) {return values()[Math.clamp(ordinal() - d, HIGHEST.ordinal(), LOWEST.ordinal())];}
    public static Talent[] settable() {return Arrays.copyOfRange(values(), HIGHEST.ordinal(), LOWEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  按权重抽一档; 权重 0 的 NONE 抽不中
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

    //  排除十绝与 NONE 的加权抽取; 用于取消十绝体质时回落
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

    //  在该档区间内 roll 一个基数
    public static int randomPercent(Talent talent) {
        if (talent.minPercent == talent.maxPercent) return talent.minPercent;
        return ThreadLocalRandom.current().nextInt(talent.minPercent, talent.maxPercent + 1);
    }

    //  反查所属档位, 无匹配返回 NONE
    public static Talent fromPercent(int percent) {
        for (Talent t : values()) {
            if (percent >= t.minPercent && percent <= t.maxPercent) return t;
        }
        return NONE;
    }
}

package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  才情: 念的自然恢复速度, 开窍时按权重抽取, 之后定终身. 见 CLAUDE.md "Wisdom"
//  ⚠ 与 GuTalent 相反, 本枚举从低到高排 —— shift(+1) 就是 ordinal + 1
public enum GuBrilliance implements StringRepresentable, EnumTranslatable {

    //  thoughtsPerSecond(每秒回念, 即每 20 tick), weight(抽取权重)
    ORDINARY   (  1, 15),
    DECENT     (  4, 25),
    DISTINCTIVE( 16, 25),
    OUTSTANDING( 64, 25),
    UNRIVALED  (256, 10);

    public static final Codec<GuBrilliance> CODEC = StringRepresentable.fromEnum(GuBrilliance::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.brilliance.";

    //  没有 NONE: 凡人也有才情, 「才情普通」是底档而不是「没有」
    public static final GuBrilliance LOWEST = ORDINARY;
    public static final GuBrilliance HIGHEST = UNRIVALED;

    private final long thoughtsPerSecond;
    private final int weight;

    GuBrilliance(long thoughtsPerSecond, int weight) {
        this.thoughtsPerSecond = thoughtsPerSecond;
        this.weight = weight;
    }

    public long getThoughtsPerSecond() {return thoughtsPerSecond;}
    public int getWeight() {return weight;}

    //  升 d 档 (正数 = 更好), 到边界即停
    public GuBrilliance shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  按权重抽一档 (15 / 25 / 25 / 25 / 10)
    public static GuBrilliance randomBrilliance() {
        int total = 0;
        for (GuBrilliance b : values()) total += b.weight;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (GuBrilliance b : values()) {
            roll -= b.weight;
            if (roll < 0) return b;
        }
        return ORDINARY;
    }
}

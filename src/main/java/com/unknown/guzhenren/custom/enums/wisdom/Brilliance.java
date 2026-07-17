package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  才情: 念的自然恢复速度, 出生时按权重抽取 (不是开窍), 之后定终身. 见 CLAUDE.md "Birth"
//  ⚠ 与 Talent 相反, 本枚举从低到高排 —— shift(+1) 就是 ordinal + 1
public enum Brilliance implements StringRepresentable, EnumTranslatable {

    //  thoughtsPerSecond(每秒回念, 即每 20 tick), weight(抽取权重)
    ORDINARY   (  1, 15),
    DECENT     (  4, 25),
    DISTINCTIVE( 16, 25),
    OUTSTANDING( 64, 25),
    UNRIVALED  (256, 10);

    public static final Codec<Brilliance> CODEC = StringRepresentable.fromEnum(Brilliance::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.brilliance.";

    //  没有 NONE: 凡人也有才情, 「才情普通」是底档而不是「没有」
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

    //  升 d 档 (正数 = 更好), 到边界即停
    public Brilliance shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}

    //  按权重抽一档 (15 / 25 / 25 / 25 / 10)
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

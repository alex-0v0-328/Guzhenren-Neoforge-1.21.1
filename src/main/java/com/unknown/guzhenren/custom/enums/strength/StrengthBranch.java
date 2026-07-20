package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  力道的三条分支, 各是一种形状:
//    HUMAN       人力钧力流 —— 数「几斤之力」, 每档九次封顶 (斤 → 十斤 → 百斤)
//    BEASTS      兽力虚影流 —— 记「受过哪几种兽力」, 一种只受一次 (一xx之力 → 百xx之力 → 千xx之力)
//    ENVIRONMENT 气象天地流 —— 未定, 只有名字
//  ⚠ 今日三者都只是称号, 没有任何效果 —— 效果日后再挂, 不要当死代码删
//  ⚠ ENVIRONMENT 至今没有数据字段, 是因为它没有设定 —— 别为它先建一个空壳
public enum StrengthBranch implements StringRepresentable, EnumTranslatable {

    HUMAN,
    BEASTS,
    ENVIRONMENT;

    public static final Codec<StrengthBranch> CODEC = StringRepresentable.fromEnum(StrengthBranch::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.strength_branch.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  人力钧力流的力, 一档一个常量: 今日只有斤, 日后十斤 百斤 各自成档
//  ⚠ MAX_PER_KIND 是「每人每档最多受九次」, 与蛊虫用几次用尽无关 —— 两个九是两回事
public enum JunStrength implements StringRepresentable, EnumTranslatable {

    JIN;

    public static final Codec<JunStrength> CODEC = StringRepresentable.fromEnum(JunStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.jun_strength.";

    public static final int MAX_PER_KIND = 9;

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

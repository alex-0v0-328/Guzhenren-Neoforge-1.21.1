package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  兽之力: 吞服兽类蛊虫改造肉身所得, 一种只能受一次. 今日只有黑白二豕, 牛象之流照此往下加
//  ⚠ 列的是「玩家能受的力」, 不是兽的种类 —— 所以 StrengthData 存的是它的集合
public enum BeastStrength implements StringRepresentable, EnumTranslatable {

    WHITE_BOAR,
    BLACK_BOAR;

    public static final Codec<BeastStrength> CODEC = StringRepresentable.fromEnum(BeastStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.beast_strength.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

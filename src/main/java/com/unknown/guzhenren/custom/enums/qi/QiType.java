package com.unknown.guzhenren.custom.enums.qi;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  气的种类. 天地人三气是升仙的门槛, 所以先做这三种 —— 后面还会有很多, 直接往下加即可
//  ⚠ 气没有上限: 肉身能承载多少气就是多少, 见 QiData
public enum QiType implements StringRepresentable, EnumTranslatable {

    HEAVEN,
    EARTH,
    HUMAN,

    //  自然道痕: 没有任何效用, 但一样计入气道道痕. 「无效果」是它的定义, 不是待办
    NATURAL;

    public static final Codec<QiType> CODEC = StringRepresentable.fromEnum(QiType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.qi.type.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

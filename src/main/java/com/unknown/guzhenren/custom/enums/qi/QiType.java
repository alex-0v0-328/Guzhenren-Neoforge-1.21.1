package com.unknown.guzhenren.custom.enums.qi;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Kinds of qi [气]. Heaven, Earth and Human are the threshold for ascension, which is why those three
//  came first; more are expected, and adding one is just another constant.
//  ⚠ Qi has no cap, deliberately -- it is an accumulator, not a pool  QiData.
public enum QiType implements StringRepresentable, EnumTranslatable {

    HEAVEN,
    EARTH,
    HUMAN,

    //  ⚠ Natural Qi does nothing and still counts toward the Qi Path's marks. Having no effect IS its
    //  definition, not a TODO.
    NATURAL;

    public static final Codec<QiType> CODEC = StringRepresentable.fromEnum(QiType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.qi.type.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

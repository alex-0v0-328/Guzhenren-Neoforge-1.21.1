package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The Human Jun Strength Branch [人力钧力流], one constant a kind: only Jin today, later Ten Jin and
//  Hundred Jin. ⚠ MAX_PER_KIND is a cap on the PLAYER (nine of each kind, ever), nothing to do with how
//  often a Gu may be used.
public enum JunStrength implements StringRepresentable, EnumTranslatable {

    JIN;

    public static final Codec<JunStrength> CODEC = StringRepresentable.fromEnum(JunStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.jun_strength.";

    public static final int MAX_PER_KIND = 9;

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

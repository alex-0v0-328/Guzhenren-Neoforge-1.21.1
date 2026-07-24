package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The Human Jun Strength Branch [人力钧力流], one constant a kind: Jin, Ten Jin, Jun (=100 Jin), Ten Jun.
//  ⚠ MAX_PER_KIND caps the PLAYER (nine layers of each kind, ever), not how often a Gu may be used.
public enum JunStrength implements StringRepresentable, EnumTranslatable {

    JIN,
    TEN_JIN,
    JUN,
    TEN_JUN;

    public static final Codec<JunStrength> CODEC = StringRepresentable.fromEnum(JunStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.jun_strength.";

    public static final int MAX_PER_KIND = 9;

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

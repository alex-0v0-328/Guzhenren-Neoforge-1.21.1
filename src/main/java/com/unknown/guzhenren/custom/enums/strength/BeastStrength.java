package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Beast strength: what swallowing a beast Gu reworks the body into, each kind taken once ever. Only
//  the two boars today; ox and elephant would follow the same pattern.
//  ⚠ These are strengths a player can take, not species -- which is why StrengthData stores a set.
public enum BeastStrength implements StringRepresentable, EnumTranslatable {

    WHITE_BOAR,
    BLACK_BOAR;

    public static final Codec<BeastStrength> CODEC = StringRepresentable.fromEnum(BeastStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.beast_strength.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

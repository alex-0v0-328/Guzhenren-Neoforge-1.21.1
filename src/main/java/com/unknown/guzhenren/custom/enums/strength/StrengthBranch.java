package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The Strength Path's [力道] three branches, each a different shape: HUMAN counts how many jin, nine
//  to a kind; BEASTS records WHICH beast strengths he took, one each ever; ENVIRONMENT is unspecced.
//  ⚠ All three are titles only today, with no effect. Not dead code -- the effects land later.
//  ⚠ ENVIRONMENT has no data field because it has no spec. Do not build it an empty one.
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

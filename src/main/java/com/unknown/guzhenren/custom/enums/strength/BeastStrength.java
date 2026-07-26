package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Beast strength: what swallowing a beast Gu reworks the body into, each kind taken once ever.
//  ⚠ These are strengths a player can take, not species -- which is why StrengthData stores a set.
public enum BeastStrength implements StringRepresentable, EnumTranslatable {

    //  ⚠⚠ The speck tag is the SPECIES', never the branch's: both boars book under 豕力, and a tiger or
    //  a bear brings its own. That is what keeps a new beast to one constant here plus one in MarkTag.
    WHITE_BOAR(MarkTag.STRENGTH_BOAR),
    BLACK_BOAR(MarkTag.STRENGTH_BOAR);

    public static final Codec<BeastStrength> CODEC = StringRepresentable.fromEnum(BeastStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.beast_strength.";

    private final MarkTag markTag;

    BeastStrength(MarkTag markTag) {this.markTag = markTag;}

    public MarkTag getMarkTag() {return markTag;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

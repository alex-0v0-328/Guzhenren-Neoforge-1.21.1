package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Beast strength: what swallowing a beast Gu reworks the body into, each kind taken once ever.
//  ⚠ These are strengths a player can take, not species -- which is why StrengthData stores a set.
public enum BeastStrength implements StringRepresentable, EnumTranslatable {

    //  Columns -- speck tag (the SPECIES', never the branch's: both boars book under 豕力), reading,
    //  attack bonus. ⚠ The bonus lands the moment the strength is taken -- the SET is what makes it once.
    //  ⚠⚠ The two boars are the ONLY constants that stack; every later beast is ONE carrying its reading.
    WHITE_BOAR(MarkTag.STRENGTH_BOAR,  1, 2.0D),
    BLACK_BOAR(MarkTag.STRENGTH_BOAR,  1, 2.0D);

    public static final Codec<BeastStrength> CODEC = StringRepresentable.fromEnum(BeastStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.beast_strength.";

    private final MarkTag markTag;
    private final int reading;
    private final double attackBonus;

    BeastStrength(MarkTag markTag, int reading, double attackBonus) {
        this.markTag = markTag;
        this.reading = reading;
        this.attackBonus = attackBonus;
    }

    public MarkTag getMarkTag() {return markTag;}
    //  How many beasts of its family one such body is worth -- 一/两/十/百/千/万, summed within the family.
    public int getReading() {return reading;}
    //  Flat attack damage this body is worth. ⚠ The species declares it, so a new beast changes no service.
    public double getAttackBonus() {return attackBonus;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

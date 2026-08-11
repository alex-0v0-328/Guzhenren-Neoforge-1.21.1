package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The beast strengths a body can hold, each declaring what it is worth.
 *
 * <p>⚠ The species declares its own mark tag and attack bonus right here, so adding one is a single
 * constant that touches neither the Gu item nor the attack sum. Do not move those to the reader.
 *
 * @author Alex
 * @since 1.0.0
 */
public enum BeastStrength implements StringRepresentable, EnumTranslatable {

    WHITE_BOAR(MarkTag.STRENGTH_BOAR,  1, 3.0D),
    BLACK_BOAR(MarkTag.STRENGTH_BOAR,  1, 3.0D),
    BEAR      (MarkTag.STRENGTH_BEAR,  1, 4.0D);

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
    public int getReading() {return reading;}
    public double getAttackBonus() {return attackBonus;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

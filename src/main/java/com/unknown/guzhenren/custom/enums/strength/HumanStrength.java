package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The Human Jun Strength Branch [人力钧力流], one constant a kind. ⚠ 一钧 = 30 斤 here, the historical
//  reading -- it was 100 until 2026-07-26, so a stale note may still say so.
public enum HumanStrength implements StringRepresentable, EnumTranslatable {

    //  Columns -- jin (斤 one layer is worth), maxLayers (the PLAYER's ceiling for this kind, ever),
    //  layersPerStep, attackBonus PER STEP. ⚠⚠ A STEP function: layers / layersPerStep * bonus, so two
    //  layers of 斤力 are worth nothing and three are worth 1. His spec, 2026-08-01.
    //  ⚠⚠ 9/9/30/30 against 1/10/30/300 tops out at exactly 9999 斤. That is why the two families differ:
    //  the total must not carry into a fifth digit.
    JIN    (  1,  9,  3,    1.0D),
    TEN_JIN( 10,  9,  3,   10.0D),
    JUN    ( 30, 30, 10,  100.0D),
    TEN_JUN(300, 30, 10, 1000.0D);

    public static final Codec<HumanStrength> CODEC = StringRepresentable.fromEnum(HumanStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.human_strength.";

    //  What a ×10 kind is worth in layers of its base kind -- the family readings are built on it.
    //  ⚠ 一钧 = 30 斤 is NOT here: it is JUN's own column, so one place declares what a layer is worth.
    public static final int TEN_FACTOR = 10;

    private final int jin;
    private final int maxLayers;
    private final int layersPerStep;
    private final double attackBonus;

    HumanStrength(int jin, int maxLayers, int layersPerStep, double attackBonus) {
        this.jin = jin;
        this.maxLayers = maxLayers;
        this.layersPerStep = layersPerStep;
        this.attackBonus = attackBonus;
    }

    public int getJin() {return jin;}
    public int getMaxLayers() {return maxLayers;}
    //  How many layers buy one step of attack damage. ⚠ 3 and 10 divide 9 and 30 exactly, so every rung
    //  is worth THREE steps and no layer is wasted. Keep that true if a ceiling ever moves.
    public int getLayersPerStep() {return layersPerStep;}
    //  Attack damage ONE step is worth. ⚠ Not the total -- see AttackService for the step arithmetic.
    public double getAttackBonus() {return attackBonus;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

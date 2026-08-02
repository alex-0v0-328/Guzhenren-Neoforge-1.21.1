package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum HumanStrength implements StringRepresentable, EnumTranslatable {

    JIN    (  1,  9,  3,  0.25D),
    TEN_JIN( 10,  9,  3,   2.5D),
    JUN    ( 30, 30, 10,   7.5D),
    TEN_JUN(300, 30, 10,  75.0D);

    public static final Codec<HumanStrength> CODEC = StringRepresentable.fromEnum(HumanStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.human_strength.";

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
    public int getLayersPerStep() {return layersPerStep;}
    public double getAttackBonus() {return attackBonus;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

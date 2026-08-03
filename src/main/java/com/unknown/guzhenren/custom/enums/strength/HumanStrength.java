package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum HumanStrength implements StringRepresentable, EnumTranslatable {

    JIN    (  1,  9),
    TEN_JIN( 10,  9),
    JUN    ( 30, 30),
    TEN_JUN(300, 30);

    public static final Codec<HumanStrength> CODEC = StringRepresentable.fromEnum(HumanStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.human_strength.";

    public static final int TEN_FACTOR = 10;

    public static final double ATTACK_PER_JIN = 0.125D;

    private final int jin;
    private final int maxLayers;

    HumanStrength(int jin, int maxLayers) {
        this.jin = jin;
        this.maxLayers = maxLayers;
    }

    public int getJin() {return jin;}
    public int getMaxLayers() {return maxLayers;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

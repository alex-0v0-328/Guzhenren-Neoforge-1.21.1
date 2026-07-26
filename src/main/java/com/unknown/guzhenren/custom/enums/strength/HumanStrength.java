package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The Human Jun Strength Branch [人力钧力流], one constant a kind. ⚠ 一钧 = 30 斤 here, the historical
//  reading -- it was 100 until 2026-07-26, so a stale note may still say so.
public enum HumanStrength implements StringRepresentable, EnumTranslatable {

    //  Columns -- jin (斤 one layer is worth), maxLayers (the PLAYER's ceiling for this kind, ever).
    //  ⚠⚠ 9/9/30/30 against 1/10/30/300 tops out at exactly 9999 斤. That is why the two families differ:
    //  the total must not carry into a fifth digit.
    JIN    (  1,  9),
    TEN_JIN( 10,  9),
    JUN    ( 30, 30),
    TEN_JUN(300, 30);

    public static final Codec<HumanStrength> CODEC = StringRepresentable.fromEnum(HumanStrength::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.human_strength.";

    //  What a ×10 kind is worth in layers of its base kind -- the family readings are built on it.
    //  ⚠ 一钧 = 30 斤 is NOT here: it is JUN's own column, so one place declares what a layer is worth.
    public static final int TEN_FACTOR = 10;

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

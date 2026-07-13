package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuStage implements StringRepresentable {

    NONE(0),
    INIT(1),
    MIDDLE(2),
    UPPER(4),
    PEAK(8);

    public static final Codec<GuStage> CODEC = StringRepresentable.fromEnum(GuStage::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.stage.";

    private final int essenceMultiplier;

    GuStage(int essenceMultiplier) {
        this.essenceMultiplier = essenceMultiplier;
    }

    public int getEssenceMultiplier() {return essenceMultiplier;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

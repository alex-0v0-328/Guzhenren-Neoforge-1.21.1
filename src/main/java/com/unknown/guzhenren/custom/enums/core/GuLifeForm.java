package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.GuTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuLifeForm implements StringRepresentable, GuTranslatable {

    MORTAL,
    IMMORTAL;

    public static final Codec<GuLifeForm> CODEC = StringRepresentable.fromEnum(GuLifeForm::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.life_form.";

    //  仙凡归属由 GuRank 直接标记 (见 GuRank.getLifeForm())

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

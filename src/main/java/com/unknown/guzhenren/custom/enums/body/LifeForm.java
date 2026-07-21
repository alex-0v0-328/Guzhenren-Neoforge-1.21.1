package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum LifeForm implements StringRepresentable, EnumTranslatable {

    MORTAL,
    IMMORTAL;

    public static final Codec<LifeForm> CODEC = StringRepresentable.fromEnum(LifeForm::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.life_form.";

    //  Which one a rank belongs to is marked on Rank itself (Rank.getLifeForm()).

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

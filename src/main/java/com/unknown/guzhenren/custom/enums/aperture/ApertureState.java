package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Whether an aperture [空窍] lives. Two states only -- the body's LifeState is the one with a third.
//  ⚠ A dead aperture draws nothing, and THIS is the gate essence regen hangs on, never the body's
//   EssenceService.regenPerDay.
public enum ApertureState implements StringRepresentable, EnumTranslatable {

    ALIVE,
    DEAD;

    public static final Codec<ApertureState> CODEC = StringRepresentable.fromEnum(ApertureState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

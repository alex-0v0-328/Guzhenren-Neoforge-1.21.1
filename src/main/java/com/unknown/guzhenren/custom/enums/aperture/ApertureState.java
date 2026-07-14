package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  空窍的生死, 只有两态 (肉身的 LifeState 才有第三态「僵」)
//  死掉的空窍吸不到天地元气 —— 这就是真元不回复的那道闸门, 见 EssenceService.regenPerDay
public enum ApertureState implements StringRepresentable, EnumTranslatable {

    ALIVE,
    DEAD;

    public static final Codec<ApertureState> CODEC = StringRepresentable.fromEnum(ApertureState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

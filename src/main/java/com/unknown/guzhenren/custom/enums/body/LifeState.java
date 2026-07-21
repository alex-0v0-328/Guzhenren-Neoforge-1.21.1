package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The body's (肉身) life state, as against LifeForm's mortal/immortal. ZOMBIFIED is the third one:
//  the person is dead and the corpse still moves.
//  ⚠ Not the same fact as the aperture's ApertureState -- the essence gate hangs there, never here.
public enum LifeState implements StringRepresentable, EnumTranslatable {

    ALIVE,
    ZOMBIE,
    DEAD;

    public static final Codec<LifeState> CODEC = StringRepresentable.fromEnum(LifeState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.life_state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The body's [肉身] life state, as against LifeForm's mortal/immortal.
//  ⚠⚠ ALIVE and DEAD, and nothing else. A ZOMBIE constant existed until 2026-08-01, when he cut
//  zombification [化僵] entirely -- "就像我从未讲过". **Do not re-add it**, and do not resurrect the
//  rule that it had to kill the apertures: there is no third state left for that rule to hang on.
//  ⚠ Not the same fact as the aperture's ApertureState -- the essence gate hangs there, never here.
public enum LifeState implements StringRepresentable, EnumTranslatable {

    ALIVE,
    DEAD;

    public static final Codec<LifeState> CODEC = StringRepresentable.fromEnum(LifeState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.life_state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

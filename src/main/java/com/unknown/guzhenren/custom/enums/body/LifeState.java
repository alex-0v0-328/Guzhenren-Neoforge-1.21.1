package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  肉身的生死状态 (区别于 LifeForm 的 凡/仙). ZOMBIE 是第三态: 人已死, 尸身还在动
//  ⚠ 与空窍的生死 (ApertureState) 是两回事: 真元的闸门挂在空窍上, 不在这里
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

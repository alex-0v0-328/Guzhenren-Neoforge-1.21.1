package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.GuTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  生死状态 (区别于 GuLifeForm 的 凡/仙). ZOMBIE 是第三态: 人已死, 尸身还在动
//  化僵者的空窍是死的, 吸不到天地元气 —— 这就是僵尸不回复真元的原因, 见 EssenceService.regenPerDay
public enum GuLifeState implements StringRepresentable, GuTranslatable {

    ALIVE,
    ZOMBIE,
    DEAD;

    public static final Codec<GuLifeState> CODEC = StringRepresentable.fromEnum(GuLifeState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.life_state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

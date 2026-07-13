package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  生死状态; 通用枚举, 但每个使用处的合法子集不同 (区别于 GuLifeForm 的 凡/仙):
//   玩家生死 (ZombieData.playerLifeState) —— 生 / 死 / 活死人(僵尸), 三态全用
//   空窍生死 (ApertureStatusData.lifeState) —— 只有 生 / 死, 空窍没有僵尸态
//  所以化僵的人空窍是「死」的, 吸不到天地元气 —— 这正是僵尸不自然回复真元的原因
//  ZOMBIE 是生与死之间的第三态: 人已经死了, 但尸身还在动 —— 档位见 GuZombieTier
public enum GuLifeState implements StringRepresentable {

    ALIVE,
    ZOMBIE,
    DEAD;

    public static final Codec<GuLifeState> CODEC = StringRepresentable.fromEnum(GuLifeState::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.life_state.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

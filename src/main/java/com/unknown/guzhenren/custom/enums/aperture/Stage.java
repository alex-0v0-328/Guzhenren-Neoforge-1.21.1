package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Stage implements StringRepresentable, EnumTranslatable {

    //  essenceMultiplier: maxEssence = baseEssence * stage.essenceMultiplier * rank.rankBase
    NONE(0),
    INIT(1),
    MIDDLE(2),
    UPPER(4),
    PEAK(8);

    public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.stage.";

    //  可设置区间: 初阶 ~ 巅峰. NONE 在下界外 —— 那是「还没开窍」, 不是一档小境界
    public static final Stage LOWEST = INIT;
    public static final Stage HIGHEST = PEAK;

    private final int essenceMultiplier;

    Stage(int essenceMultiplier) {
        this.essenceMultiplier = essenceMultiplier;
    }

    public int getEssenceMultiplier() {return essenceMultiplier;}

    //  升降 d 档, 到边界即停 —— 巅峰再 up 不会破境, 破境是玩法, 不是命令的事
    public Stage shift(int d) {return values()[Math.clamp(ordinal() + d, LOWEST.ordinal(), HIGHEST.ordinal())];}
    public static Stage[] settable() {return Arrays.copyOfRange(values(), LOWEST.ordinal(), HIGHEST.ordinal() + 1);}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

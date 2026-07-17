package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuAttainment implements StringRepresentable, EnumTranslatable {

    //  NONE 是默认境界 (尚未入门), 排在普通之下
    NONE(0),
    ORDINARY(1),
    QUASI_MASTER(2),
    MASTER(3),
    QUASI_GRANDMASTER(4),
    GRANDMASTER(5),
    QUASI_GREAT_GRANDMASTER(6),
    GREAT_GRANDMASTER(7),
    QUASI_SUPREME_GRANDMASTER(8),
    SUPREME_GRANDMASTER(9);

    public static final Codec<GuAttainment> CODEC = StringRepresentable.fromEnum(GuAttainment::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.attainment.";

    private final int level;

    GuAttainment(int level) {
        this.level = level;
    }

    public int getLevel() {return level;}

    //  升降 d 档, 到边界即停. 本枚举从低到高排, +1 就是升一档
    //  下界是 NONE 而非 ORDINARY: 「尚未入门」是一个合法的造诣, 降回去有意义
    public GuAttainment shift(int d) {return values()[Math.clamp(ordinal() + d, 0, values().length - 1)];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuPathAttainment implements StringRepresentable {

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

    public static final Codec<GuPathAttainment> CODEC = StringRepresentable.fromEnum(GuPathAttainment::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.attainment.";

    private final int level;

    GuPathAttainment(int level) {
        this.level = level;
    }

    public int getLevel() {return level;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

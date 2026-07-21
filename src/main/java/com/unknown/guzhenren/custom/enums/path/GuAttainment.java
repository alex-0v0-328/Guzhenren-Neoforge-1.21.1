package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuAttainment implements StringRepresentable, EnumTranslatable {

    //  NONE is the default: not yet a beginner, one below Ordinary.
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

    //  Shift d grades, stopping at the edge. These run LOW to HIGH, so +1 really is ordinal + 1.
    //  ⚠ The floor is NONE, not ORDINARY: "not yet a beginner" is a real attainment to fall back to.
    public GuAttainment shift(int d) {return values()[Math.clamp(ordinal() + d, 0, values().length - 1)];}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

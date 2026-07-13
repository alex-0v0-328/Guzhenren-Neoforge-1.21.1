package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Path implements StringRepresentable {

    HEAVEN, RULE, SPACE, TIME, HUMAN,

    METAL, WOOD, WATER, FIRE, EARTH, ICE_SNOW, LIGHTNING,
    QI, SOUND, LIGHT, DARK,

    STRENGTH, DREAM, REFINEMENT, WISDOM, THEFT,
    LUCK, KILLING, BLOOD, SOUL, ENSLAVEMENT,
    FOOD, FORMATION, PAINTING, TRANSFORMATION;

    public static final Codec<Path> CODEC = StringRepresentable.fromEnum(Path::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.path.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuPath implements StringRepresentable, EnumTranslatable {

    HEAVEN, RULE, SPACE, TIME, HUMAN,

    METAL, WOOD, WATER, FIRE, EARTH, ICE_SNOW, LIGHTNING,
    QI, SOUND, LIGHT, DARK,

    STRENGTH, DREAM, REFINEMENT, WISDOM, THEFT,
    LUCK, KILLING, BLOOD, SOUL, ENSLAVEMENT,
    FOOD, FORMATION, PAINTING, TRANSFORMATION;

    public static final Codec<GuPath> CODEC = StringRepresentable.fromEnum(GuPath::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.path.";

    //  ⚠ A featured path (肉体特殊流派): its MARKS come from a sub-system (QiData) with its own
    //  breakdown, not from PathData. ⚠⚠ Specks stay ordinary here; light one up only once that
    //  sub-system exists  CLAUDE.md "Featured paths".
    private static final Set<GuPath> FEATURED = EnumSet.of(QI);

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
    public boolean isFeatured() {return FEATURED.contains(this);}
}

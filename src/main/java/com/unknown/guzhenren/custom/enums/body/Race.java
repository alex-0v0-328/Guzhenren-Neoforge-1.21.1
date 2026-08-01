package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Race implements StringRepresentable, EnumTranslatable {

    HUMAN       (null),

    HAIRY_MEN   (GuPath.REFINEMENT),
    EGGMEN      (GuPath.SPACE),
    ROCKMEN     (GuPath.EARTH),
    FEATHERMEN  (GuPath.CLOUD),
    INKMEN      (GuPath.INFORMATION),
    MINIMEN     (GuPath.WOOD),
    MERMEN      (GuPath.WATER),
    BEASTMEN    (GuPath.TRANSFORMATION),
    DRAGONMEN   (GuPath.ENSLAVEMENT),
    MUSHROOMMEN (GuPath.POISON),
    SNOWMEN     (GuPath.ICE_SNOW);

    public static final long TALENT_MARKS = 10L;
    public static final GuAttainment TALENT_ATTAINMENT = GuAttainment.MASTER;

    public static final Codec<Race> CODEC = StringRepresentable.fromEnum(Race::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.race.";

    private final @Nullable GuPath talentPath;

    Race(@Nullable GuPath talentPath) {this.talentPath = talentPath;}

    public @Nullable GuPath talentPath() {return talentPath;}

    public boolean isVariant() {return talentPath != null;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

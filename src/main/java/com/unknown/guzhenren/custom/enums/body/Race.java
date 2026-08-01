package com.unknown.guzhenren.custom.enums.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  What a body is. ⚠ Everyone is born HUMAN [人族]; a Variant Human [异人] is only ever MADE, by command
//  or item -- there is no roll at birth, so onBirth says nothing about race.
public enum Race implements StringRepresentable, EnumTranslatable {

    //  ⚠ Not GuPath.HUMAN [人道] -- a different enum in a different package, and a different word.
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

    //  What being this race is worth on its talent path. ⚠ The marks are booked under MarkTag.RACE so a
    //  change revokes exactly them; the attainment only ever RISES -- see BodyService.setRace.
    public static final long TALENT_MARKS = 10L;
    public static final GuAttainment TALENT_ATTAINMENT = GuAttainment.MASTER;

    public static final Codec<Race> CODEC = StringRepresentable.fromEnum(Race::values);
    private static final String KEY_PREFIX = "guzhenren.enum.body.race.";

    private final @Nullable GuPath talentPath;

    Race(@Nullable GuPath talentPath) {this.talentPath = talentPath;}

    //  Null for HUMAN -- an ordinary human is born owing nothing to any path.
    public @Nullable GuPath talentPath() {return talentPath;}

    //  ⚠ Variant Human [异人] is DERIVED, never a constant: having a talent path IS what makes one. A
    //  second enum whose only job is that grouping earns nothing today -- unlike StrengthBranch, which
    //  exists because 兼修互斥 really needs the branch level.
    public boolean isVariant() {return talentPath != null;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

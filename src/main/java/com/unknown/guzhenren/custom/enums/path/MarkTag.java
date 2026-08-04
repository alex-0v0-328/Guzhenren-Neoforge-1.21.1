package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MarkTag implements StringRepresentable, EnumTranslatable {

    NATURAL        (null),

    RACE           (null),

    QI_HEAVEN      (GuPath.QI),
    QI_EARTH       (GuPath.QI),
    QI_HUMAN       (GuPath.QI),
    QI_NATURAL     (GuPath.QI),
    QI_DEATH       (GuPath.QI),
    QI_SWORD       (GuPath.QI),
    QI_LIFE        (GuPath.QI),
    QI_ESSENCE     (GuPath.QI),
    QI_STRENGTH    (GuPath.QI),

    //  TODO(互斥): 兼修 penalty -- 1+1 should land near 1.5, not 2.  CLAUDE.md "Pending".
    STRENGTH_BEASTS(GuPath.STRENGTH),
    STRENGTH_BOAR  (GuPath.STRENGTH),
    STRENGTH_BEAR  (GuPath.STRENGTH),
    STRENGTH_HUMAN (GuPath.STRENGTH);

    public static final Codec<MarkTag> CODEC = StringRepresentable.fromEnum(MarkTag::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.tag.";

    private final @Nullable GuPath owner;

    MarkTag(@Nullable GuPath owner) {this.owner = owner;}

    public boolean fitsOn(GuPath path) {return owner == null || owner == path;}
    public @Nullable GuPath owner() {return owner;}

    public @Nullable MarkTag parent() {
        return switch (this) {
            case STRENGTH_BOAR, STRENGTH_BEAR -> STRENGTH_BEASTS;
            default -> null;
        };
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

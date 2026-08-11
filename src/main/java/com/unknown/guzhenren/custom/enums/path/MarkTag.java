package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import java.util.Arrays;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Where a Dao mark [道痕] or speck [碎屑] came from, so a quantity can later be revoked exactly.
 *
 * <p>⚠ A tag declares which path owns it, and a write under any other path is dropped silently rather
 * than refused. That is what makes a wrongly tagged write look like it worked.
 *
 * <p>☠ A tag whose quantity another system revokes wholesale must not be settable, or a hand-written
 * amount is silently overwritten by that system and cannot be told from what it granted.
 *
 * @author Alex
 * @since 1.0.0
 */
public enum MarkTag implements StringRepresentable, EnumTranslatable {

    NATURAL        (null,            true ),

    RACE           (null,            false),

    //  TODO(互斥): 兼修 penalty -- 1+1 should land near 1.5, not 2.  CLAUDE.md "Pending".
    STRENGTH_BEASTS(GuPath.STRENGTH, true ),
    STRENGTH_BOAR  (GuPath.STRENGTH, true ),
    STRENGTH_BEAR  (GuPath.STRENGTH, true ),
    STRENGTH_HUMAN (GuPath.STRENGTH, true );

    public static final Codec<MarkTag> CODEC = StringRepresentable.fromEnum(MarkTag::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.tag.";

    private final @Nullable GuPath owner;
    private final boolean settable;

    MarkTag(@Nullable GuPath owner, boolean settable) {
        this.owner = owner;
        this.settable = settable;
    }

    public boolean fitsOn(GuPath path) {return owner == null || owner == path;}
    public boolean isSettable() {return settable;}
    public @Nullable GuPath owner() {return owner;}

    /** The tags a command may write on this path, which is a subset of the ones that fit on it. */
    public static MarkTag[] settableOn(GuPath path) {
        return Arrays.stream(values())
                .filter(tag -> tag.settable && tag.fitsOn(path))
                .toArray(MarkTag[]::new);
    }

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

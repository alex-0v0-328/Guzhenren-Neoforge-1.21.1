package com.unknown.guzhenren.custom.enums.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  Where a mark [道痕] or speck [碎屑] came from, so a quantity can be revoked or converted instead of
//  blindly subtracted. ⚠ Closed enum -- no sibling mod adds one. See CLAUDE.md "道痕/碎屑 tags".
public enum MarkTag implements StringRepresentable, EnumTranslatable {

    //  ⚠ Universal, and what anything with no source lands in: it counts toward the total and does nothing
    //  else. That is a definition, not a TODO -- it is exactly what the old QiType.NATURAL always meant.
    NATURAL        (null),

    //  What a Variant Human [异人] is born owing to its talent path. ⚠ Universal like NATURAL, because the
    //  path is the RACE's to name -- but its OWN tag, so changing race revokes exactly the ten it laid
    //  down instead of eating marks the player earned on that path himself.
    RACE           (null),

    //  Qi Path [气道]. Heaven, Earth and Human are the threshold for ascension, which is why they came
    //  first. ⚠ The five below arrived with the qi materials and complete the namespace -- see ModItems.
    QI_HEAVEN      (GuPath.QI),
    QI_EARTH       (GuPath.QI),
    QI_HUMAN       (GuPath.QI),
    QI_NATURAL     (GuPath.QI),
    QI_DEATH       (GuPath.QI),
    QI_SWORD       (GuPath.QI),
    QI_LIFE        (GuPath.QI),
    QI_ESSENCE     (GuPath.QI),
    QI_STRENGTH    (GuPath.QI),

    //  Strength Path [力道]. ⚠⚠ TWO LEVELS on the beast side: STRENGTH_BOAR is a KIND of STRENGTH_BEASTS
    //  (see parent()), because the coming 互斥 check weighs sub-branches of one path against each other.
    //  ⚠⚠ Only the species tag is WRITTEN -- speck() sums the tag maps, so booking a use under both would
    //  make one boar use read as two specks. The parent is the grouping, not a second quantity.
    //  TODO(互斥): 兼修 penalty -- 1+1 should land near 1.5, not 2.  CLAUDE.md "Pending".
    STRENGTH_BEASTS(GuPath.STRENGTH),
    STRENGTH_BOAR  (GuPath.STRENGTH),
    STRENGTH_HUMAN (GuPath.STRENGTH);

    public static final Codec<MarkTag> CODEC = StringRepresentable.fromEnum(MarkTag::values);
    private static final String KEY_PREFIX = "guzhenren.enum.path.tag.";

    private final @Nullable GuPath owner;

    MarkTag(@Nullable GuPath owner) {this.owner = owner;}

    //  Null owner means every path takes it. This one check is the whole "dead qi cannot land on the
    //  Fire Path" rule -- PathData enforces it at the door, so a mismatch is unrepresentable.
    public boolean fitsOn(GuPath path) {return owner == null || owner == path;}
    public @Nullable GuPath owner() {return owner;}

    //  The broader tag this one is a KIND of -- 豕力 is a 兽力虚影流, and the coming 互斥 check walks up
    //  it. ⚠ A method, not a ctor column: an enum constant may not name another in its own arguments.
    public @Nullable MarkTag parent() {return this == STRENGTH_BOAR ? STRENGTH_BEASTS : null;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

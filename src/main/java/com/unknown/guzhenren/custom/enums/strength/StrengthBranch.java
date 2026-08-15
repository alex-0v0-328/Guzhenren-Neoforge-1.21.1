package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The branches strength splits into, for a mutual-exclusion rule that is not built yet.
 *
 * <p>Closed vocabulary enum; {@code HUMAN}/{@code BEASTS} drive attack damage, {@code ENVIRONMENT} and
 * {@code OLDER_ANTIQUITY} carry no data by design. No sibling mod may add a branch.
 *
 * <p>⚠ {@code OLDER_ANTIQUITY} is deliberate, not a mistranslation -- do not "correct" it to
 * {@code ANCIENT}. Not every branch feeds attack damage; treating the branch as a uniform multiplier
 * would be wrong about the presentation-only ones.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see HumanStrength
 * @see BeastStrength
 */
public enum StrengthBranch implements StringRepresentable, EnumTranslatable {

    HUMAN,
    BEASTS,
    ENVIRONMENT,
    OLDER_ANTIQUITY;

    public static final Codec<StrengthBranch> CODEC = StringRepresentable.fromEnum(StrengthBranch::values);
    private static final String KEY_PREFIX = "guzhenren.enum.strength.strength_branch.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.strength;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The branches strength splits into, for a mutual-exclusion rule that is not built yet.
 *
 * <p>⚠ Not every branch feeds attack damage; one of them is presentation for now. Treating the branch
 * as a uniform multiplier would be wrong about that one.
 *
 * @author Alex
 * @since 1.0.0
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

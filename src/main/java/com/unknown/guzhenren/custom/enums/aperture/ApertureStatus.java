package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The aperture status [空窍状态]: what an aperture [空窍] still can and cannot do, derived never stored.
 *
 * <p>Closed vocabulary enum. NORMAL covers every living holder -- alive and half-zombie [半生半僵]
 * alike (the half rate is a speed, not a status); DEAD [死窍] is the zombie's aperture -- no natural
 * regen, no nourishing, no striking; STONE [石窍] is what the Stone Aperture Gu [石窍蛊] leaves --
 * the same three refusals, permanently.
 *
 * <p>⚠ The derivation lives on {@code ApertureService#status}, not here -- enums in this package are
 * vocabulary, and the check every gate wants is {@code status == NORMAL}.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public enum ApertureStatus implements StringRepresentable, EnumTranslatable {

    NORMAL, DEAD, STONE;

    public static final Codec<ApertureStatus> CODEC = StringRepresentable.fromEnum(ApertureStatus::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.status.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

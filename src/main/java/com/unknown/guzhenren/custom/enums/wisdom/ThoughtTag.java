package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The composition of a thought [念] pool: which kind a portion is tagged as.
 *
 * <p>⚠ NATURAL is derived, never stored -- it is what remains once every tagged portion is subtracted
 * from the pool's current. Only EVIL (and future tags) live in the map, so NATURAL has no writer.
 *
 * @author Alex
 * @since 1.0.0
 */
public enum ThoughtTag implements StringRepresentable, EnumTranslatable {

    NATURAL,
    EVIL;

    public static final Codec<ThoughtTag> CODEC = StringRepresentable.fromEnum(ThoughtTag::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.tag.";

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    @Override
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The kinds of thought [念] a mind holds, each with its own capacity.
 *
 * <p>⚠ Only some may be overfilled past the cap, and only those can ever be lethal.
 * {@code isBurstable()} is the single declaration of which; never re-test the constant at a call site.
 *
 * @author Alex
 * @since 1.0.0
 */
public enum WisdomType implements StringRepresentable, EnumTranslatable {

    THOUGHTS(50_000L, true),
    WILLS   (    12L, false),
    EMOTIONS(     8L, false);

    public static final Codec<WisdomType> CODEC = StringRepresentable.fromEnum(WisdomType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.type.";

    public static final long BURST_NUMERATOR = 6L;
    public static final long BURST_DENOMINATOR = 5L;

    private final long defaultCapacity;
    private final boolean burstable;

    WisdomType(long defaultCapacity, boolean burstable) {
        this.defaultCapacity = defaultCapacity;
        this.burstable = burstable;
    }

    public long getDefaultCapacity() {return defaultCapacity;}
    public boolean isBurstable() {return burstable;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

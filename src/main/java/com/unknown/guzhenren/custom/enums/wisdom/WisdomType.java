package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The three cells of the Mind Ocean (脑海): Thoughts -> Wills -> Emotions, each condensed from the one
//  before. ⚠ That conversion is not built  CLAUDE.md "Pending".
//  The number is the starting capacity; overflowing it is what shatters the Mind Ocean  MindPool.
public enum WisdomType implements StringRepresentable, EnumTranslatable {

    THOUGHTS(30_000L),
    WILLS(5L),
    EMOTIONS(2L);

    public static final Codec<WisdomType> CODEC = StringRepresentable.fromEnum(WisdomType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.type.";

    //  Starting capacity. ⚠ Stored, not derived: a Gu or a pill may raise the cap later.
    private final long defaultCapacity;

    WisdomType(long defaultCapacity) {
        this.defaultCapacity = defaultCapacity;
    }

    public long getDefaultCapacity() {return defaultCapacity;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

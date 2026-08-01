package com.unknown.guzhenren.custom.enums.wisdom;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  The three cells of the Mind Ocean [脑海]: Thoughts -> Wills -> Emotions, each condensed from the one
//  before. ⚠ That conversion is not built  CLAUDE.md "Pending".
//  The number is the starting capacity; overflowing it is what shatters the Mind Ocean  MindPool.
//  ⚠⚠ TWO SHAPES, and only one of them can kill (his 1.0.0 spec, 2026-08-01): 念 may be overfilled by a
//  fifth and BURSTS past that, while 意 and 情 are HARD CAPS -- a full one simply takes no more, and
//  neither is ever lethal. `burstable` is what says which.
public enum WisdomType implements StringRepresentable, EnumTranslatable {

    //       capacity, burstable
    THOUGHTS(50_000L, true),
    WILLS   (    12L, false),
    EMOTIONS(     8L, false);

    public static final Codec<WisdomType> CODEC = StringRepresentable.fromEnum(WisdomType::values);
    private static final String KEY_PREFIX = "guzhenren.enum.wisdom.type.";

    //  ⚠⚠ A fifth over the cap is the whole buffer, and past it the Mind Ocean shatters. It was 2× until
    //  his 1.0.0 spec landed (2026-08-01) -- the numerator/denominator pair is the ONE place it is written.
    public static final long BURST_NUMERATOR = 6L;
    public static final long BURST_DENOMINATOR = 5L;

    //  Starting capacity. ⚠ Stored, not derived: a Gu or a pill may raise the cap later.
    private final long defaultCapacity;
    private final boolean burstable;

    WisdomType(long defaultCapacity, boolean burstable) {
        this.defaultCapacity = defaultCapacity;
        this.burstable = burstable;
    }

    public long getDefaultCapacity() {return defaultCapacity;}
    //  Whether this cell may be filled past its cap at all. ⚠ False means a HARD cap, never a death.
    public boolean isBurstable() {return burstable;}

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

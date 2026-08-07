package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Title implements StringRepresentable, EnumTranslatable {

    MORTAL,
    GU_MASTER,
    GU_IMMORTAL;

    public static final Codec<Title> CODEC = StringRepresentable.fromEnum(Title::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.title.";

    public static @NotNull Title fromRank(Rank rank) {
        if (rank == Rank.NONE) return MORTAL;
        return rank.ordinal() > Rank.HIGHEST.ordinal() ? GU_IMMORTAL : GU_MASTER;
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

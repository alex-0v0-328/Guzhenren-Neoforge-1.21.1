package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

//  Essence [真元] colour, one a rank -- the mapping lives on Rank. shade(stage) darkens it by stage;
//  the immortal ranks take a fixed one.
//  ⚠ Nothing calls this yet: the HUD uses one fixed blue ( CLAUDE.md "Client"). It is the hook a
//  particle, item or screen will take -- NOT dead code. Delete it and the enum is a bag of names.
public enum EssenceColor implements StringRepresentable, EnumTranslatable {

    //  Mortal / unawakened: grey.
    NONE(0xFF808080, false),

    //  Rank I..V (mortal): the value is the Middle-stage colour; Initial lightens it, Peak darkens it.
    GREEN_COPPER(0xFF3EC98A),
    RED_STEEL(0xFFD9503F),
    WHITE_SILVER(0xFFCCCCCC),
    YELLOW_GOLDEN(0xFFE8BE43),
    PURPLE_CRYSTAL(0xFFA855D4),

    //  Rank VI..IX (immortal): fixed, never shaded.
    GREEN_GRAPE(0xFF9ACD32, false),
    RED_DATE(0xFF8B2500, false),
    WHITE_LITCHI(0xFFF5F0E1, false),
    YELLOW_APRICOT(0xFFFBCEB1, false);

    public static final Codec<EssenceColor> CODEC = StringRepresentable.fromEnum(EssenceColor::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.essence_color.";

    //  Brightness per stage, indexed by Stage.ordinal(): none, Initial, Middle, Upper, Peak. One line
    //  retunes the whole palette.
    private static final float[] STAGE_BRIGHTNESS = {1.00F, 1.20F, 1.00F, 0.72F, 0.45F};

    private final int baseColor;
    private final boolean shadeByStage;

    EssenceColor(int baseColor) {
        this(baseColor, true);
    }

    EssenceColor(int baseColor, boolean shadeByStage) {
        this.baseColor = baseColor;
        this.shadeByStage = shadeByStage;
    }

    //  Base colour (ARGB): the Middle-stage shade where stages apply, the final colour where they do not.
    public int getBaseColor() {return baseColor;}

    //  The actual colour at that stage (ARGB). Alpha is untouched; only RGB scales.
    public int shade(@NotNull Stage stage) {
        if (!shadeByStage) return baseColor;

        float factor = STAGE_BRIGHTNESS[stage.ordinal()];
        int alpha = baseColor >>> 24;
        int red = scale((baseColor >> 16) & 0xFF, factor);
        int green = scale((baseColor >> 8) & 0xFF, factor);
        int blue = scale(baseColor & 0xFF, factor);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int scale(int channel, float factor) {
        return Math.clamp(Math.round(channel * factor), 0, 255);
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

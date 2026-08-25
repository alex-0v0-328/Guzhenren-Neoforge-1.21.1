package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The Ten-Extremes [十绝] physiques, and the tables that hang off each of them.
 *
 * <p>Closed vocabulary enum on {@code ApertureData}. A physique other than {@code NONE} exists exactly
 * when the aptitude is the top grade -- one fact held in two places, kept in step only by the aperture
 * service. No sibling mod may add a physique.
 *
 * <p>⚠ The birth die skips {@code PURE_DREAM_REALITY_SEEKER} ({@code randomTenExtreme} excludes it): it
 * translates and is settable but can never be rolled. Read the "after" physique only AFTER {@code enforce}.
 *
 * @author Alex
 * @version 1.0.0
 * @see GuPath
 * @since 1.0.0
 */

public enum ExtremePhysique implements StringRepresentable, EnumTranslatable {

    NONE(100, 0),
    VERDANT_GREAT_SUN(100, 50, GuPath.SPACE),
    DESOLATE_ANCIENT_MOON(100, 50, GuPath.TIME),
    NORTHERN_DARK_ICE_SOUL(100, 50, GuPath.ICE_SNOW, GuPath.SOUL),
    BOUNDLESS_FOREST_SAMSARA(100, 50, GuPath.WOOD),
    BLAZING_GLORY_LIGHTNING_BRILLIANCE(100, 50, GuPath.FIRE, GuPath.LIGHTNING),
    MYRIAD_GOLD_WONDROUS_ESSENCE(100, 50, GuPath.METAL),
    GREAT_STRENGTH_TRUE_MARTIAL(300, 150, GuPath.STRENGTH),
    CAREFREE_WISDOM_HEART(100, 50, GuPath.WISDOM),
    PROFOUND_EARTH_ORIGIN(100, 50, GuPath.EARTH),
    UNIVERSE_GREAT_DERIVATION(100, 50, GuPath.RULE),
    PURE_DREAM_REALITY_SEEKER(100, 50, GuPath.DREAM);

    public static final Codec<ExtremePhysique> CODEC = StringRepresentable.fromEnum(ExtremePhysique::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.extreme_physique.";

    private final int strengthCapacity;
    private final int staminaMaxPercent;
    private final List<GuPath> talentPaths;

    ExtremePhysique(int strengthCapacity, int staminaMaxPercent, GuPath... talentPaths) {
        this.strengthCapacity = strengthCapacity;
        this.staminaMaxPercent = staminaMaxPercent;
        this.talentPaths = List.of(talentPaths);
    }

    public int getStrengthCapacity() {return strengthCapacity;}
    public int getStaminaMaxPercent() {return staminaMaxPercent;}
    public List<GuPath> getTalentPaths() {return talentPaths;}

    public static ExtremePhysique randomTenExtreme() {
        List<ExtremePhysique> pool = new ArrayList<>();
        for (ExtremePhysique p : values()) {
            if (p != NONE && p != PURE_DREAM_REALITY_SEEKER) pool.add(p);
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

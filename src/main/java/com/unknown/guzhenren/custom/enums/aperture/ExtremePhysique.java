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
 * <p>⚠ A physique other than NONE exists exactly when the aptitude is the top grade. They are one fact
 * held in two places, and only the aperture service keeps them in step.
 *
 * @author Alex
 * @since 1.0.0
 */
public enum ExtremePhysique implements StringRepresentable, EnumTranslatable {

    NONE                              (100, 100,  5),
    VERDANT_GREAT_SUN                 (100, 150, 10, GuPath.SPACE),
    DESOLATE_ANCIENT_MOON             (100, 150, 10, GuPath.TIME),
    NORTHERN_DARK_ICE_SOUL            (100, 150, 10, GuPath.ICE_SNOW, GuPath.SOUL),
    BOUNDLESS_FOREST_SAMSARA          (100, 150, 10, GuPath.WOOD),
    BLAZING_GLORY_LIGHTNING_BRILLIANCE(100, 150, 10, GuPath.FIRE, GuPath.LIGHTNING),
    MYRIAD_GOLD_WONDROUS_ESSENCE      (100, 150, 10, GuPath.METAL),
    GREAT_STRENGTH_TRUE_MARTIAL       (300, 250, 25, GuPath.STRENGTH),
    CAREFREE_WISDOM_HEART             (100, 150, 10, GuPath.WISDOM),
    PROFOUND_EARTH_ORIGIN             (100, 150, 10, GuPath.EARTH),
    UNIVERSE_GREAT_DERIVATION         (100, 150, 10, GuPath.RULE),
    PURE_DREAM_REALITY_SEEKER         (100, 150, 10, GuPath.DREAM);

    public static final Codec<ExtremePhysique> CODEC = StringRepresentable.fromEnum(ExtremePhysique::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.extreme_physique.";

    private final int strengthCapacity;
    private final int staminaBase;
    private final int staminaRegen;
    private final List<GuPath> talentPaths;

    ExtremePhysique(int strengthCapacity, int staminaBase, int staminaRegen, GuPath... talentPaths) {
        this.strengthCapacity = strengthCapacity;
        this.staminaBase = staminaBase;
        this.staminaRegen = staminaRegen;
        this.talentPaths = List.of(talentPaths);
    }

    public int getStrengthCapacity() {return strengthCapacity;}
    public int getStaminaBase() {return staminaBase;}
    public int getStaminaRegen() {return staminaRegen;}
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

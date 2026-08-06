package com.unknown.guzhenren.custom.enums.aperture;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.EnumTranslatable;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ExtremePhysique implements StringRepresentable, EnumTranslatable {

    NONE                              (100),
    VERDANT_GREAT_SUN                 (100, GuPath.SPACE),
    DESOLATE_ANCIENT_MOON             (100, GuPath.TIME),
    NORTHERN_DARK_ICE_SOUL            (100, GuPath.ICE_SNOW, GuPath.SOUL),
    BOUNDLESS_FOREST_SAMSARA          (100, GuPath.WOOD),
    BLAZING_GLORY_LIGHTNING_BRILLIANCE(100, GuPath.FIRE, GuPath.LIGHTNING),
    MYRIAD_GOLD_WONDROUS_ESSENCE      (100, GuPath.METAL),
    GREAT_STRENGTH_TRUE_MARTIAL       (300, GuPath.STRENGTH),
    CAREFREE_WISDOM_HEART             (100, GuPath.WISDOM),
    PROFOUND_EARTH_ORIGIN             (100, GuPath.EARTH),
    UNIVERSE_GREAT_DERIVATION         (100, GuPath.RULE),
    PURE_DREAM_REALITY_SEEKER         (100, GuPath.DREAM);

    public static final Codec<ExtremePhysique> CODEC = StringRepresentable.fromEnum(ExtremePhysique::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.extreme_physique.";

    private final int strengthCapacity;
    private final List<GuPath> talentPaths;

    ExtremePhysique(int strengthCapacity, GuPath... talentPaths) {
        this.strengthCapacity = strengthCapacity;
        this.talentPaths = List.of(talentPaths);
    }

    public int getStrengthCapacity() {return strengthCapacity;}
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

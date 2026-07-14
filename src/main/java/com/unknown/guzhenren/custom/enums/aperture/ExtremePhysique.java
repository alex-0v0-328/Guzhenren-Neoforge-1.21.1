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

    //  talentPaths: 该体质的天赋流派 (十绝体质自带的道)
    NONE                              (),
    VERDANT_GREAT_SUN                 (GuPath.SPACE),
    DESOLATE_ANCIENT_MOON             (GuPath.TIME),
    NORTHERN_DARK_ICE_SOUL            (GuPath.WATER, GuPath.ICE_SNOW),
    BOUNDLESS_FOREST_SAMSARA          (GuPath.WOOD),
    BLAZING_GLORY_LIGHTNING_BRILLIANCE(GuPath.FIRE, GuPath.LIGHTNING),
    MYRIAD_GOLD_WONDROUS_ESSENCE      (GuPath.METAL),
    GREAT_STRENGTH_TRUE_MARTIAL       (GuPath.STRENGTH),
    CAREFREE_WISDOM_HEART             (GuPath.WISDOM),
    PROFOUND_EARTH_ORIGIN             (GuPath.EARTH),
    UNIVERSE_GREAT_DERIVATION         (GuPath.RULE),
    PURE_DREAM_REALITY_SEEKER         (GuPath.DREAM);

    public static final Codec<ExtremePhysique> CODEC = StringRepresentable.fromEnum(ExtremePhysique::values);
    private static final String KEY_PREFIX = "guzhenren.enum.aperture.extreme_physique.";

    private final List<GuPath> talentPaths;

    ExtremePhysique(GuPath... talentPaths) {
        this.talentPaths = List.of(talentPaths);
    }

    public List<GuPath> getTalentPaths() {return talentPaths;}

    //  随机一个十绝体质: 排除 NONE 与 PURE_DREAM_REALITY_SEEKER, 其余十个等概率
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

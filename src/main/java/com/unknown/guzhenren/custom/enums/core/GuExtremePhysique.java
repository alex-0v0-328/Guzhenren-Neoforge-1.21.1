package com.unknown.guzhenren.custom.enums.core;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.path.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuExtremePhysique implements StringRepresentable {

    NONE,
    VERDANT_GREAT_SUN(Path.SPACE),
    DESOLATE_ANCIENT_MOON(Path.TIME),
    NORTHERN_DARK_ICE_SOUL(Path.WATER, Path.ICE_SNOW),
    BOUNDLESS_FOREST_SAMSARA(Path.WOOD),
    BLAZING_GLORY_LIGHTNING_BRILLIANCE(Path.FIRE, Path.LIGHTNING),
    MYRIAD_GOLD_WONDROUS_ESSENCE(Path.METAL),
    GREAT_STRENGTH_TRUE_MARTIAL(Path.STRENGTH),
    CAREFREE_WISDOM_HEART(Path.WISDOM),
    PROFOUND_EARTH_ORIGIN(Path.EARTH),
    UNIVERSE_GREAT_DERIVATION(Path.RULE),
    PURE_DREAM_REALITY_SEEKER(Path.DREAM);

    public static final Codec<GuExtremePhysique> CODEC = StringRepresentable.fromEnum(GuExtremePhysique::values);
    private static final String KEY_PREFIX = "guzhenren.enum.core.ten_extreme.";

    private final List<Path> talentPaths;

    GuExtremePhysique(Path... talentPaths) {
        this.talentPaths = List.of(talentPaths);
    }

    public List<Path> getTalentPaths() {return talentPaths;}

    //  随机一个十绝体质: 排除 NONE 与 PURE_DREAM_REALITY_SEEKER, 其余十个等概率
    public static GuExtremePhysique randomTenExtreme() {
        List<GuExtremePhysique> pool = new ArrayList<>();
        for (GuExtremePhysique p : values()) {
            if (p != NONE && p != PURE_DREAM_REALITY_SEEKER) pool.add(p);
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Override
    public @NotNull String getSerializedName() {return name().toLowerCase();}
    public String getTranslationKey() {return KEY_PREFIX + name().toLowerCase();}
}

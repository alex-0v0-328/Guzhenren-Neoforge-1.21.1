package com.unknown.guzhenren.client;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Draws a graded effect's icon from its amplifier, so one effect can wear a different face per grade.
 *
 * <p>Implements {@link com.unknown.guzhenren.client.EffectIconLayout} as a record carrying a name, a
 * directory ({@code mob_effect} or {@code item}), and the rank range. The texture path is
 * {@code directory/name_rank}, where rank is {@code amplifier + 1} clamped to the range. Two factories
 * ({@code mobEffect} / {@code item}) select which texture set to read from.
 *
 * <p>⚠ This exists so a family of grades stays a single effect. Splitting it into one effect per
 * grade would let a player hold two grades of the same thing at the same time.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.client.EffectIconLayout
 * @see com.unknown.guzhenren.client.ItemEffectIcon
 */
public record GradedEffectIcon(String name, String directory, int lowestRank, int highestRank) implements EffectIconLayout {

    public static GradedEffectIcon mobEffect(String name, int lowestRank, int highestRank) {
        return new GradedEffectIcon(name, "mob_effect", lowestRank, highestRank);
    }
    public static GradedEffectIcon item(String name, int lowestRank, int highestRank) {
        return new GradedEffectIcon(name, "item", lowestRank, highestRank);
    }

    @Override
    public String textureFor(MobEffectInstance instance) {
        int rank = Mth.clamp(instance.getAmplifier() + 1, lowestRank, highestRank);
        return directory + "/" + name + "_" + rank;
    }
}

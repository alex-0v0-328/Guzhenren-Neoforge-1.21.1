package com.unknown.guzhenren.datagen.world;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Writes the biome tags deciding where a wild Gu [野生蛊虫] may spawn.
 *
 * <p>Extends {@link net.minecraft.data.tags.TagsProvider} for {@link net.minecraft.world.level.biome.Biome}.
 * Lists all 39 land biomes one by one under {@code hope_gu_spawns}; must NOT collapse to
 * {@code #minecraft:is_overworld} because that carries the oceans, whose surface sits at sea level.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */
public class ModBiomeTagsProvider extends TagsProvider<Biome> {

    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, lookupProvider, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModBiomeTags.HOPE_GU_SPAWNS)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.MEADOW)
                .add(Biomes.CHERRY_GROVE)
                .add(Biomes.FLOWER_FOREST)

                .add(Biomes.FOREST)
                .add(Biomes.BIRCH_FOREST)
                .add(Biomes.DARK_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.WINDSWEPT_FOREST)

                .add(Biomes.TAIGA)
                .add(Biomes.SNOWY_TAIGA)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                .add(Biomes.GROVE)

                .add(Biomes.SAVANNA)
                .add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.WINDSWEPT_SAVANNA)

                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE)
                .add(Biomes.BAMBOO_JUNGLE)

                .add(Biomes.DESERT)
                .add(Biomes.BADLANDS)
                .add(Biomes.WOODED_BADLANDS)
                .add(Biomes.ERODED_BADLANDS)

                .add(Biomes.SNOWY_PLAINS)
                .add(Biomes.ICE_SPIKES)
                .add(Biomes.SNOWY_SLOPES)
                .add(Biomes.FROZEN_PEAKS)
                .add(Biomes.JAGGED_PEAKS)
                .add(Biomes.STONY_PEAKS)

                .add(Biomes.WINDSWEPT_HILLS)
                .add(Biomes.WINDSWEPT_GRAVELLY_HILLS)

                .add(Biomes.SWAMP)
                .add(Biomes.MANGROVE_SWAMP)

                .add(Biomes.BEACH)
                .add(Biomes.SNOWY_BEACH)
                .add(Biomes.STONY_SHORE)

                .add(Biomes.MUSHROOM_FIELDS);
    }
}

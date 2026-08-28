package com.unknown.guzhenren.registry.world;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The biome tags this mod declares.
 *
 * <p>Tag-key holder (not a DeferredRegister): {@code HOPE_GU_SPAWNS} lists the land biomes wild Gu
 * spawn in. It must NOT collapse to {@code #minecraft:is_overworld} -- that carries the oceans, whose
 * surface sits at sea level where the height check cannot hold a mote off the water.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public final class ModBiomeTags {

    private ModBiomeTags() {}

    public static final TagKey<Biome> HOPE_GU_SPAWNS = key("hope_gu_spawns");

    private static TagKey<Biome> key(String name) {
        return TagKey.create(Registries.BIOME, Guzhenren.id(name));
    }
}

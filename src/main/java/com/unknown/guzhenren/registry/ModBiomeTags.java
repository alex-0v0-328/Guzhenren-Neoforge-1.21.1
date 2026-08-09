package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomeTags {

    private ModBiomeTags() {}

    public static final TagKey<Biome> HOPE_GU_SPAWNS = key("hope_gu_spawns");

    private static TagKey<Biome> key(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, name));
    }
}

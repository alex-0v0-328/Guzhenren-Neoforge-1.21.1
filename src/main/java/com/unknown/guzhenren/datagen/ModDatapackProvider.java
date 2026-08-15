package com.unknown.guzhenren.datagen;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModBiomeTags;
import com.unknown.guzhenren.registry.ModDamageTypes;
import com.unknown.guzhenren.registry.ModEntityTypes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The single provider for every datapack registry this mod writes.
 *
 * <p>Extends {@link net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider}. Builds damage
 * types and biome modifiers in one {@code RegistrySetBuilder}. The tag providers take
 * {@code getRegistryProvider()} from this instance, not the plain lookup, so the tag pass sees the
 * types this run generates.
 *
 * <p>⚠ There can only be one. The builtin-entries provider reports a fixed name, so a second instance
 * fails datagen outright; add a registry to this one's builder instead.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */
public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDatapackProvider::damageTypes)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModDatapackProvider::biomeModifiers);

    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Guzhenren.MOD_ID));
    }

    //region Damage types [伤害类型]
    private static void damageTypes(BootstrapContext<DamageType> context) {
        context.register(ModDamageTypes.LIFESPAN_EXHAUSTED, new DamageType("guzhenren.lifespan_exhausted", 0.0F));
        context.register(ModDamageTypes.SOUL_COLLAPSE, new DamageType("guzhenren.soul_collapse", 0.0F));
        context.register(ModDamageTypes.MIND_OCEAN_SHATTERED, new DamageType("guzhenren.mind_ocean_shattered", 0.0F));
        context.register(ModDamageTypes.VITAL_GU_LOST, new DamageType("guzhenren.vital_gu_lost", 0.0F));
    }
    //endregion

    //region Biome modifiers [生态修改] -- where a wild Gu [野生蛊虫] spawns
    private static final ResourceKey<BiomeModifier> SPAWN_HOPE_GU = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            Guzhenren.id("spawn_hope_gu"));

    private static final int SPAWN_WEIGHT = 8;
    private static final int PACK_MINIMUM = 1;
    private static final int PACK_MAXIMUM = 2;

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(SPAWN_HOPE_GU, new BiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(ModBiomeTags.HOPE_GU_SPAWNS),
                List.of(new MobSpawnSettings.SpawnerData(
                        ModEntityTypes.HOPE_GU_ENTITY.get(), SPAWN_WEIGHT, PACK_MINIMUM, PACK_MAXIMUM))));
    }
    //endregion
}

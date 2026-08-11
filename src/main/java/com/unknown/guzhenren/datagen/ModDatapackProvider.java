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
 * <p>⚠ There can only be one. The builtin-entries provider reports a fixed name, so a second instance
 * fails datagen outright; add a registry to this one's builder instead.
 *
 * @author Alex
 * @since 1.0.0
 */
//    ⚠⚠ Every datapack registry this mod writes goes through THIS ONE provider. DatapackBuiltinEntriesProvider
//    reports the fixed name "Registries", so a second instance fails the run with "Duplicate provider".
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
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "spawn_hope_gu"));

    //    ⚠ The pack size is what a player actually SEES: the category cap (15) bounds the total either way,
    //    but a drift of 2..4 reads far denser than fifteen lone specks spread over 128 blocks.
    //    ⚠⚠ 4 is the ceiling -- Mob.getMaxSpawnClusterSize() truncates anything larger mid-spawn.
    //    ⚠ Weight does nothing above sea level: bats never spawn there, so nothing shares the category.
    private static final int SPAWN_WEIGHT = 8;
    private static final int PACK_MINIMUM = 2;
    private static final int PACK_MAXIMUM = 4;

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(SPAWN_HOPE_GU, new BiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(ModBiomeTags.HOPE_GU_SPAWNS),
                List.of(new MobSpawnSettings.SpawnerData(
                        ModEntityTypes.HOPE_GU_ENTITY.get(), SPAWN_WEIGHT, PACK_MINIMUM, PACK_MAXIMUM))));
    }
    //endregion
}

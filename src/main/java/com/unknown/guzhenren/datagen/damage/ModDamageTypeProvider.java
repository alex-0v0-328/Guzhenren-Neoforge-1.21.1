package com.unknown.guzhenren.datagen.damage;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModDamageTypes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

//  Writes data/guzhenren/damage_type/*.json. That is the whole job.
//
//  This is NOT where the damage types are registered. DamageType is a *datapack* registry: there is
//  no DeferredRegister to write to and nothing happens at startup. The registration side is
//  registry/ModDamageTypes, which owns the two ResourceKeys; the values behind those keys are plain
//  JSON that the game loads together with the world, and a DataProvider is simply what authors that
//  JSON for us during `runData`. This class does not exist at runtime at all -- which is exactly why
//  it lives in datagen/ next to the language providers, and not in registry/.
//
//  The msgId is what builds the death message key: "guzhenren.lifespan_exhausted" becomes
//  "death.attack.guzhenren.lifespan_exhausted", which both lang providers translate.
public class ModDamageTypeProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::damageTypes);

    public ModDamageTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Guzhenren.MOD_ID));
    }

    private static void damageTypes(BootstrapContext<DamageType> context) {
        //  No exhaustion: starving to death on top of dying of old age would just be noise.
        context.register(ModDamageTypes.LIFESPAN_EXHAUSTED, new DamageType("guzhenren.lifespan_exhausted", 0.0F));
        context.register(ModDamageTypes.SOUL_COLLAPSE, new DamageType("guzhenren.soul_collapse", 0.0F));
    }
}

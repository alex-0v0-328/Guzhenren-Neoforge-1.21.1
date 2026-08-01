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

public class ModDamageTypeProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::damageTypes);

    public ModDamageTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Guzhenren.MOD_ID));
    }

    private static void damageTypes(BootstrapContext<DamageType> context) {
        context.register(ModDamageTypes.LIFESPAN_EXHAUSTED, new DamageType("guzhenren.lifespan_exhausted", 0.0F));
        context.register(ModDamageTypes.SOUL_COLLAPSE, new DamageType("guzhenren.soul_collapse", 0.0F));
        context.register(ModDamageTypes.MIND_OCEAN_SHATTERED, new DamageType("guzhenren.mind_ocean_shattered", 0.0F));
        context.register(ModDamageTypes.VITAL_GU_LOST, new DamageType("guzhenren.vital_gu_lost", 0.0F));
    }
}

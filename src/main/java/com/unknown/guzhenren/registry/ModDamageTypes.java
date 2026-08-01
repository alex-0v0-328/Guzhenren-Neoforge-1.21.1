package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class ModDamageTypes {

    private ModDamageTypes() {}

    public static final ResourceKey<DamageType> LIFESPAN_EXHAUSTED = key("lifespan_exhausted");

    public static final ResourceKey<DamageType> SOUL_COLLAPSE = key("soul_collapse");

    public static final ResourceKey<DamageType> MIND_OCEAN_SHATTERED = key("mind_ocean_shattered");

    public static final ResourceKey<DamageType> VITAL_GU_LOST = key("vital_gu_lost");

    public static DamageSource source(Entity entity, ResourceKey<DamageType> type) {
        return new DamageSource(entity.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(type));
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, name));
    }
}

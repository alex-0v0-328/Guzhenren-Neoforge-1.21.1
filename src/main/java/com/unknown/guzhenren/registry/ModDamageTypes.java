package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

//  Keys only. DamageType is a datapack registry -- JSON written at runData by datagen/damage.
public final class ModDamageTypes {

    private ModDamageTypes() {}

    //  寿元耗尽而亡
    public static final ResourceKey<DamageType> LIFESPAN_EXHAUSTED = key("lifespan_exhausted");

    //  魂魄衰竭而亡
    public static final ResourceKey<DamageType> SOUL_COLLAPSE = key("soul_collapse");

    //  脑海炸裂而亡 -- 念/意/情 over capacity, see CLAUDE.md "Wisdom".
    public static final ResourceKey<DamageType> MIND_OCEAN_SHATTERED = key("mind_ocean_shattered");

    //  Registry off the entity, not entity.level() -- Level-AutoCloseable gotcha. See CLAUDE.md "Gotcha".
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

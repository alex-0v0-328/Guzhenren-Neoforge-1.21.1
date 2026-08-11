package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.entity.FlyingGuEntity;
import com.unknown.guzhenren.entity.HopeGuEntity;
import com.unknown.guzhenren.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Where this mod's entities declare their attributes and where they are allowed to spawn.
 *
 * @author Alex
 * @since 1.0.0
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class EntityRegistrationEvents {

    private EntityRegistrationEvents() {}

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.HOPE_GU_ENTITY.get(), FlyingGuEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntityTypes.HOPE_GU_ENTITY.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityRegistrationEvents::onTheSurface,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean onTheSurface(EntityType<HopeGuEntity> type, ServerLevelAccessor level,
                                        MobSpawnType reason, BlockPos pos, RandomSource random) {
        return pos.getY() >= level.getSeaLevel();
    }
}

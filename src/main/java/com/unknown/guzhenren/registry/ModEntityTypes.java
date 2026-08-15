package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.entity.HopeGuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The entity types this mod registers.
 *
 * <p>DeferredRegister holder: owns the {@link HopeGuEntity} ambient mote
 * ({@code MobCategory.AMBIENT}, {@code END_ROD} particle). The catch is a bare right click and is never
 * gated on awakening [开窍].
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see HopeGuEntity
 */
public final class ModEntityTypes {

    private ModEntityTypes() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Guzhenren.MOD_ID);

    private static final float MOTE_WIDTH  = 0.4F;
    private static final float MOTE_HEIGHT = 0.4F;

    private static final int TRACKING_CHUNKS = 8;

    public static final DeferredHolder<EntityType<?>, EntityType<HopeGuEntity>> HOPE_GU_ENTITY =
            ENTITY_TYPES.register("hope_gu_entity", () -> EntityType.Builder
                    .<HopeGuEntity>of((type, level) ->
                            new HopeGuEntity(type, level, ModItems.HOPE_GU, ParticleTypes.END_ROD),
                            MobCategory.AMBIENT)
                    .sized(MOTE_WIDTH, MOTE_HEIGHT)
                    .clientTrackingRange(TRACKING_CHUNKS)
                    .build("hope_gu_entity"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}

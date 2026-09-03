package com.unknown.guzhenren.registry.entity;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.entity.BoarGuEntity;
import com.unknown.guzhenren.entity.HopeGuEntity;
import com.unknown.guzhenren.registry.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The entity types this mod registers.
 *
 * <p>Deferred holders register Hope Gu [希望蛊] and three boar Gu [豕蛊] variants as ambient entities. The boar
 * holders are summon-only and never enter natural spawning; Hope Gu's client mote is emitted by its entity class.
 * The catch is a bare right click and is never gated on awakening [开窍].
 *
 * @author Alex
 * @version 1.0.0
 * @see HopeGuEntity
 * @since 1.0.0
 */

public final class ModEntityTypes {

    private ModEntityTypes() {}
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Guzhenren.MOD_ID);
    private static final float MOTE_WIDTH = 0.4F;
    private static final float MOTE_HEIGHT = 0.4F;
    private static final int TRACKING_CHUNKS = 8;
    public static final DeferredHolder<EntityType<?>, EntityType<HopeGuEntity>> HOPE_GU_ENTITY =
            ENTITY_TYPES.register("hope_gu_entity", () -> EntityType.Builder
                    .<HopeGuEntity>of((type, level) ->
                                    new HopeGuEntity(type, level, ModItems.HOPE_GU),
                            MobCategory.AMBIENT)
                    .sized(MOTE_WIDTH, MOTE_HEIGHT)
                    .clientTrackingRange(TRACKING_CHUNKS)
                    .build("hope_gu_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<BoarGuEntity>> WHITE_BOAR_GU_ENTITY =
            ENTITY_TYPES.register("white_boar_gu_entity", () -> EntityType.Builder
                    .<BoarGuEntity>of((type, level) -> new BoarGuEntity(type, level, ModItems.WHITE_BOAR_GU),
                            MobCategory.AMBIENT)
                    .sized(MOTE_WIDTH, MOTE_HEIGHT)
                    .clientTrackingRange(TRACKING_CHUNKS)
                    .build("white_boar_gu_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<BoarGuEntity>> BLACK_BOAR_GU_ENTITY =
            ENTITY_TYPES.register("black_boar_gu_entity", () -> EntityType.Builder
                    .<BoarGuEntity>of((type, level) -> new BoarGuEntity(type, level, ModItems.BLACK_BOAR_GU),
                            MobCategory.AMBIENT)
                    .sized(MOTE_WIDTH, MOTE_HEIGHT)
                    .clientTrackingRange(TRACKING_CHUNKS)
                    .build("black_boar_gu_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<BoarGuEntity>> FLOWER_BOAR_GU_ENTITY =
            ENTITY_TYPES.register("flower_boar_gu_entity", () -> EntityType.Builder
                    .<BoarGuEntity>of((type, level) -> new BoarGuEntity(type, level, ModItems.FLOWER_BOAR_GU),
                            MobCategory.AMBIENT)
                    .sized(MOTE_WIDTH, MOTE_HEIGHT)
                    .clientTrackingRange(TRACKING_CHUNKS)
                    .build("flower_boar_gu_entity"));
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}

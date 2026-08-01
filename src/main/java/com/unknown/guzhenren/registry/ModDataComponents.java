package com.unknown.guzhenren.registry;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.RefinedGuState;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Guzhenren.MOD_ID);

    public static final Supplier<DataComponentType<RefinedGuState>> REFINED_GU_STATE =
            DATA_COMPONENTS.registerComponentType("refined_gu_state", builder -> builder
                    .persistent(RefinedGuState.CODEC)
                    .networkSynchronized(RefinedGuState.STREAM_CODEC));

    public static final Supplier<DataComponentType<UUID>> VITAL_OWNER =
            DATA_COMPONENTS.registerComponentType("vital_owner", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static final Supplier<DataComponentType<Long>> STORED_STONES =
            DATA_COMPONENTS.registerComponentType("stored_stones", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final Supplier<DataComponentType<Long>> FED_AT =
            DATA_COMPONENTS.registerComponentType("fed_at", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final Supplier<DataComponentType<Long>> FED_WARNED =
            DATA_COMPONENTS.registerComponentType("fed_warned", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final Supplier<DataComponentType<Integer>> FED_LEFT =
            DATA_COMPONENTS.registerComponentType("fed_left", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}

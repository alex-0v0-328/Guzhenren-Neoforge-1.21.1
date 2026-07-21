package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.RefinedGuState;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Per-stack item state: a Gu's refinement, and whose Vital Gu it is.
//  ⚠ networkSynchronized is not a packet -- it rides the vanilla stack sync.  CLAUDE.md "Networking".
public final class ModDataComponents {

    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Guzhenren.MOD_ID);

    //  ⚠ Replaced boar_state + jin_state, which were byte-identical. A save from before 2026-07-21
    //  keeps those old components, so an existing boar Gu reads back as wild.
    public static final Supplier<DataComponentType<RefinedGuState>> REFINED_GU_STATE =
            DATA_COMPONENTS.registerComponentType("refined_gu_state", builder -> builder
                    .persistent(RefinedGuState.CODEC)
                    .networkSynchronized(RefinedGuState.STREAM_CODEC));

    //  Whose Vital Gu (本命蛊) this is. ⚠ The owner's UUID, not a flag: a refined Gu may be handed to another
    //  player, and the loss must always be billed to the one who bound it. Never cleared once set.
    public static final Supplier<DataComponentType<UUID>> VITAL_OWNER =
            DATA_COMPONENTS.registerComponentType("vital_owner", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}

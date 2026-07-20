package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.RefinedGuState;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Per-stack item state. One today: a boar Gu's refinement, uses and hunger.
//  ⚠ networkSynchronized is not a packet -- it rides the vanilla stack sync. See CLAUDE.md "Networking".
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

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}

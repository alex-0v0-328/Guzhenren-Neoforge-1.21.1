package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.mortal.BoarState;
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

    public static final Supplier<DataComponentType<BoarState>> BOAR_STATE =
            DATA_COMPONENTS.registerComponentType("boar_state", builder -> builder
                    .persistent(BoarState.CODEC)
                    .networkSynchronized(BoarState.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}

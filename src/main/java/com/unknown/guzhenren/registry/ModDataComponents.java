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

//  Per-stack item state: a Gu's refinement [炼化], whose Vital Gu [本命蛊] it is, and what it stores.
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

    //  Whose Vital Gu [本命蛊] this is. ⚠ The owner's UUID, not a flag: a refined Gu may be handed to another
    //  player, and the loss must always be billed to the one who bound it. Never cleared once set.
    public static final Supplier<DataComponentType<UUID>> VITAL_OWNER =
            DATA_COMPONENTS.registerComponentType("vital_owner", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    //  Primeval Stones [元石] held inside a Primeval Elder Gu [元老蛊], which eats its own upkeep out of them.
    //  ⚠ A bare long, like VITAL_OWNER: one number, so a record around it would be pure ceremony.
    public static final Supplier<DataComponentType<Long>> STORED_STONES =
            DATA_COMPONENTS.registerComponentType("stored_stones", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    //  The overworld day-time tick a fed-clock Gu [酒虫 · 元老蛊] last ate at. ⚠⚠ The TIMESTAMP is the whole
    //  state -- there is no decay to bill, so nothing can double-count, lose a remainder or mis-handle a
    //  relog. ⚠ A bare long and NOT a field on RefinedGuState, which every refinable Gu shares: the
    //  STORED_STONES precedent. Only the two families that run this clock ever carry it.
    public static final Supplier<DataComponentType<Long>> FED_AT =
            DATA_COMPONENTS.registerComponentType("fed_at", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    //  Which feeding the 「蛊饿了」 warning was already spoken for -- it stores that meal's FED_AT value.
    //  ⚠⚠ The fed clock is read every second, so without this latch the warning would repeat every second
    //  once the window lapsed. ⚠ Feeding rewrites FED_AT, which un-latches it for free: no clearing step,
    //  and no way for the two to fall out of step.
    public static final Supplier<DataComponentType<Long>> FED_WARNED =
            DATA_COMPONENTS.registerComponentType("fed_warned", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    //  How many bar units a fed-clock Gu has left before it starves. ⚠⚠ A DERIVED value, not the truth --
    //  FED_AT is. A bar method is handed nothing but the stack, so the client cannot read the world clock
    //  itself; the server, which reads it every second anyway, writes the answer down here.
    //  ⚠ Refreshed only when it CHANGES, which at 1000 ticks a unit is once every fifty seconds -- a
    //  per-second component write would push a stack sync every second, per Gu.
    public static final Supplier<DataComponentType<Integer>> FED_LEFT =
            DATA_COMPONENTS.registerComponentType("fed_left", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}

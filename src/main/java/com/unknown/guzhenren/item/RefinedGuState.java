package com.unknown.guzhenren.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  Per-stack state of any refinable Gu: how far refined, how far toward the next payout, how fed.
//  ⚠ The limits are NOT here -- they belong to the item (RefinableGuItem), which clamps on write.
//  This record only guarantees nothing goes negative.
public record RefinedGuState(int refineProgress, int useCount, int hunger) {

    public static final RefinedGuState WILD = new RefinedGuState(0, 0, 0);

    public static final Codec<RefinedGuState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("refine_progress", 0).forGetter(RefinedGuState::refineProgress),
            Codec.INT.optionalFieldOf("use_count", 0).forGetter(RefinedGuState::useCount),
            Codec.INT.optionalFieldOf("hunger", 0).forGetter(RefinedGuState::hunger)
    ).apply(instance, RefinedGuState::new));

    public static final StreamCodec<ByteBuf, RefinedGuState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RefinedGuState::refineProgress,
            ByteBufCodecs.VAR_INT, RefinedGuState::useCount,
            ByteBufCodecs.VAR_INT, RefinedGuState::hunger,
            RefinedGuState::new);

    public RefinedGuState {
        refineProgress = Math.max(0, refineProgress);
        useCount = Math.max(0, useCount);
        hunger = Math.max(0, hunger);
    }

    public RefinedGuState withRefine(int v) {return new RefinedGuState(v, useCount, hunger);}
    public RefinedGuState withUses(int v) {return new RefinedGuState(refineProgress, v, hunger);}
    public RefinedGuState withHunger(int v) {return new RefinedGuState(refineProgress, useCount, v);}
}

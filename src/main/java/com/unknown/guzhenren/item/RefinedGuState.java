package com.unknown.guzhenren.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RefinedGuState(boolean refined, int refineProgress, int investedEssence, int hunger) {

    public static final RefinedGuState WILD = new RefinedGuState(false, 0, 0, 0);

    public static final Codec<RefinedGuState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("refined", false).forGetter(RefinedGuState::refined),
            Codec.INT.optionalFieldOf("refine_progress", 0).forGetter(RefinedGuState::refineProgress),
            Codec.INT.optionalFieldOf("invested_essence", 0).forGetter(RefinedGuState::investedEssence),
            Codec.INT.optionalFieldOf("hunger", 0).forGetter(RefinedGuState::hunger)
    ).apply(instance, RefinedGuState::new));

    public static final StreamCodec<ByteBuf, RefinedGuState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RefinedGuState::refined,
            ByteBufCodecs.VAR_INT, RefinedGuState::refineProgress,
            ByteBufCodecs.VAR_INT, RefinedGuState::investedEssence,
            ByteBufCodecs.VAR_INT, RefinedGuState::hunger,
            RefinedGuState::new);

    public RefinedGuState {
        refineProgress = Math.max(0, refineProgress);
        investedEssence = Math.max(0, investedEssence);
        hunger = Math.max(0, hunger);
    }

    public RefinedGuState withRefine(int v) {return new RefinedGuState(refined, v, investedEssence, hunger);}
    public RefinedGuState withInvested(int v) {return new RefinedGuState(refined, refineProgress, v, hunger);}
    public RefinedGuState withHunger(int v) {return new RefinedGuState(refined, refineProgress, investedEssence, v);}
}

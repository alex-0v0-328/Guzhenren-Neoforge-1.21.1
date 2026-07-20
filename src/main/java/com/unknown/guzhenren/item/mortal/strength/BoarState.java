package com.unknown.guzhenren.item.mortal.strength;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  Per-stack state of a boar Gu (豕蛊): how far it is refined, how often it was used, how fed it is.
//  ⚠ WILD's hunger is 0, which is also starvation -- every reader must ask refined() first.
public record BoarState(int refineProgress, int useCount, int hunger) {

    public static final int REFINE_COST = 1000;
    public static final int MAX_HUNGER = 18;

    //  ⚠ Uses per grant, NOT a lifetime. The Gu is never consumed -- see grantDue/afterGrant.
    public static final int USES_PER_GRANT = 36;

    public static final BoarState WILD = new BoarState(0, 0, 0);

    public static final Codec<BoarState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("refine_progress", 0).forGetter(BoarState::refineProgress),
            Codec.INT.optionalFieldOf("use_count", 0).forGetter(BoarState::useCount),
            Codec.INT.optionalFieldOf("hunger", 0).forGetter(BoarState::hunger)
    ).apply(instance, BoarState::new));

    public static final StreamCodec<ByteBuf, BoarState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BoarState::refineProgress,
            ByteBufCodecs.VAR_INT, BoarState::useCount,
            ByteBufCodecs.VAR_INT, BoarState::hunger,
            BoarState::new);

    public BoarState {
        refineProgress = Math.clamp(refineProgress, 0, REFINE_COST);
        useCount = Math.clamp(useCount, 0, USES_PER_GRANT);
        hunger = Math.clamp(hunger, 0, MAX_HUNGER);
    }

    //  Bound to an owner. Derived: a stored flag could contradict the progress that earned it.
    //  ⚠ Starving is the ONLY way a refined Gu ends. Nothing else consumes it.
    public boolean refined() {return refineProgress >= REFINE_COST;}
    public boolean starved() {return refined() && hunger <= 0;}
    public boolean grantDue() {return useCount >= USES_PER_GRANT;}
    public BoarState fed(int points) {return new BoarState(refineProgress, useCount, hunger + points);}
    public BoarState used() {return new BoarState(refineProgress, useCount + 1, hunger - 1);}
    public BoarState afterGrant() {return new BoarState(refineProgress, 0, hunger);}
    public BoarState decayed(int days) {return new BoarState(refineProgress, useCount, hunger - days);}

    //  Invest essence; completing it hands back a half-fed Gu, so the owner starts on the feeding clock.
    public BoarState refine(int essence) {
        int next = refineProgress + essence;
        return next >= REFINE_COST ? new BoarState(REFINE_COST, 0, MAX_HUNGER / 2) : new BoarState(next, 0, 0);
    }
}

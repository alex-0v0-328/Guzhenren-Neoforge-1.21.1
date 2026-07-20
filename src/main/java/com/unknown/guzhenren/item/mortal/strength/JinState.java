package com.unknown.guzhenren.item.mortal.strength;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  Per-stack state of a jin strength Gu (斤力蛊): how far it is refined, how often used, how fed.
//  ⚠ WILD's hunger is 0, which is also starvation -- every reader must ask refined() first.
//  TODO(refactor): identical in shape to BoarState. Merge at the third such Gu -- doing it now would cost
//  either the compact ctor's clamps (the limits differ) or a data component id migration.
public record JinState(int refineProgress, int useCount, int hunger) {

    public static final int REFINE_COST = 1000;
    public static final int MAX_HUNGER = 18;

    //  ⚠ Uses per grant, NOT a lifetime. The Gu is never consumed -- see grantDue/afterGrant.
    public static final int USES_PER_GRANT = 36;

    public static final JinState WILD = new JinState(0, 0, 0);

    public static final Codec<JinState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("refine_progress", 0).forGetter(JinState::refineProgress),
            Codec.INT.optionalFieldOf("use_count", 0).forGetter(JinState::useCount),
            Codec.INT.optionalFieldOf("hunger", 0).forGetter(JinState::hunger)
    ).apply(instance, JinState::new));

    public static final StreamCodec<ByteBuf, JinState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, JinState::refineProgress,
            ByteBufCodecs.VAR_INT, JinState::useCount,
            ByteBufCodecs.VAR_INT, JinState::hunger,
            JinState::new);

    public JinState {
        refineProgress = Math.clamp(refineProgress, 0, REFINE_COST);
        useCount = Math.clamp(useCount, 0, USES_PER_GRANT);
        hunger = Math.clamp(hunger, 0, MAX_HUNGER);
    }

    //  Bound to an owner. Derived: a stored flag could contradict the progress that earned it.
    public boolean refined() {return refineProgress >= REFINE_COST;}
    //  ⚠ Starving is the ONLY way a refined Gu ends. Nothing else consumes it.
    public boolean starved() {return refined() && hunger <= 0;}
    public boolean grantDue() {return useCount >= USES_PER_GRANT;}
    public JinState fed(int points) {return new JinState(refineProgress, useCount, hunger + points);}
    public JinState used() {return new JinState(refineProgress, useCount + 1, hunger - 1);}
    public JinState afterGrant() {return new JinState(refineProgress, 0, hunger);}
    public JinState decayed(int days) {return new JinState(refineProgress, useCount, hunger - days);}

    //  Invest essence; completing it hands back a half-fed Gu, so the owner starts on the feeding clock.
    public JinState refine(int essence) {
        int next = refineProgress + essence;
        return next >= REFINE_COST ? new JinState(REFINE_COST, 0, MAX_HUNGER / 2) : new JinState(next, 0, 0);
    }
}

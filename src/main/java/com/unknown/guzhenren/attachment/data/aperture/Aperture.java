package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One mortal aperture (凡窍): what a cultivator *is*, plus the essence pool that hangs off it.
//  A player holds 0..2 of them.
//  ⚠ The cap is derived here, so currentEssence is clamped structurally -- no writer can exceed it.
public record Aperture(
        Rank rank,
        Stage stage,
        int baseEssence,
        ExtremePhysique extremePhysique,
        long currentEssence,
        ApertureState state
) {

    //  Aptitude base: 20..100. Only NONE is 0 -- a real aperture never is. See ApertureService.
    public static final int MIN_BASE = 20;
    public static final int MAX_BASE = 100;

    //  The read fallback for "no aperture there". Never stored: unawakened is an empty list, not this.
    public static final Aperture NONE = new Aperture(
            Rank.NONE, Stage.NONE, 0, ExtremePhysique.NONE, 0L, ApertureState.ALIVE);

    //  All optional: a field added later must not invalidate old saves.
    public static final Codec<Aperture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rank.CODEC.optionalFieldOf("rank", Rank.NONE).forGetter(Aperture::rank),
            Stage.CODEC.optionalFieldOf("stage", Stage.NONE).forGetter(Aperture::stage),
            Codec.INT.optionalFieldOf("base_essence", 0).forGetter(Aperture::baseEssence),
            ExtremePhysique.CODEC.optionalFieldOf("extreme_physique", ExtremePhysique.NONE)
                    .forGetter(Aperture::extremePhysique),
            Codec.LONG.optionalFieldOf("current_essence", 0L).forGetter(Aperture::currentEssence),
            ApertureState.CODEC.optionalFieldOf("state", ApertureState.ALIVE).forGetter(Aperture::state)
    ).apply(instance, Aperture::new));

    public static final StreamCodec<ByteBuf, Aperture> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(Rank.class), Aperture::rank,
            ModStreamCodecs.ofEnum(Stage.class), Aperture::stage,
            ByteBufCodecs.VAR_INT, Aperture::baseEssence,
            ModStreamCodecs.ofEnum(ExtremePhysique.class), Aperture::extremePhysique,
            ByteBufCodecs.VAR_LONG, Aperture::currentEssence,
            ModStreamCodecs.ofEnum(ApertureState.class), Aperture::state,
            Aperture::new);

    //  ⚠ 1..19 is a hole, not a value: no talent tier there, but a live essence cap. Snap out of it.
    public Aperture {
        baseEssence = baseEssence <= 0 ? 0 : Math.clamp(baseEssence, MIN_BASE, MAX_BASE);
        currentEssence = Math.clamp(currentEssence, 0L, maxEssence(rank, stage, baseEssence));
    }

    //  Awakening (开窍): Rank I Initial + a rolled tier + its physique, and a full pool.
    public static Aperture opened() {
        Talent talent = Talent.randomTalent();
        ExtremePhysique physique = talent == Talent.EXTREME
                ? ExtremePhysique.randomTenExtreme()
                : ExtremePhysique.NONE;

        int base = Talent.randomPercent(talent);
        long max = maxEssence(Rank.ONE, Stage.INIT, base);
        return new Aperture(Rank.ONE, Stage.INIT, base, physique, max, ApertureState.ALIVE);
    }

    //  ---- derived, never stored; see CLAUDE.md ----
    //  ⚠ Rank.SIX..NINE have rankBase == 0, so an immortal caps at 0. Deliberate -- do not "fix" it.
    public static long maxEssence(Rank rank, Stage stage, int base) {
        return Math.max(0L, (long) base * stage.getEssenceMultiplier() * rank.getRankBase());
    }

    public long maxEssence() {return maxEssence(rank, stage, baseEssence);}
    public Talent talent() {return Talent.fromPercent(baseEssence);}
    public boolean isExtreme() {return talent() == Talent.EXTREME;}
    public boolean isAlive() {return state == ApertureState.ALIVE;}
    public Aperture refilled() {return withCurrentEssence(maxEssence());}

    //  ---- withers ----  six components: a one-liner would run past 120 columns
    public Aperture withRank(Rank v) {
        return new Aperture(v, stage, baseEssence, extremePhysique, currentEssence, state);
    }
    public Aperture withStage(Stage v) {
        return new Aperture(rank, v, baseEssence, extremePhysique, currentEssence, state);
    }
    public Aperture withBaseEssence(int v) {
        return new Aperture(rank, stage, v, extremePhysique, currentEssence, state);
    }
    public Aperture withExtremePhysique(ExtremePhysique v) {
        return new Aperture(rank, stage, baseEssence, v, currentEssence, state);
    }
    public Aperture withCurrentEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, v, state);
    }
    public Aperture withState(ApertureState v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, v);
    }
}

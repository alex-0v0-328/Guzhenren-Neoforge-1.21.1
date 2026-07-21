package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  One mortal aperture (凡窍): what a cultivator *is*, plus the essence pool that hangs off it.
//  A player holds 0..2 of them.
//  ⚠ The cap is derived here, so currentEssence is clamped structurally -- no writer can exceed it.
//  ⚠ Both paths are NULLABLE -- the data model's only nulls. GuPath has no NONE, and "has not chosen"
//  is a real state. See CLAUDE.md "Primary and secondary path".
public record Aperture(
        Rank rank,
        Stage stage,
        int baseEssence,
        ExtremePhysique extremePhysique,
        long currentEssence,
        ApertureState state,
        @Nullable GuPath primaryPath,
        @Nullable GuPath secondaryPath
) {

    //  Aptitude base: 20..100. Only NONE is 0 -- a real aperture never is. See ApertureService.
    public static final int MIN_BASE = 20;
    public static final int MAX_BASE = 100;

    //  The read fallback for "no aperture there". Never stored: unawakened is an empty list, not this.
    public static final Aperture NONE = new Aperture(
            Rank.NONE, Stage.NONE, 0, ExtremePhysique.NONE, 0L, ApertureState.ALIVE, null, null);

    //  All optional: a field added later must not invalidate old saves.
    public static final Codec<Aperture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rank.CODEC.optionalFieldOf("rank", Rank.NONE).forGetter(Aperture::rank),
            Stage.CODEC.optionalFieldOf("stage", Stage.NONE).forGetter(Aperture::stage),
            Codec.INT.optionalFieldOf("base_essence", 0).forGetter(Aperture::baseEssence),
            ExtremePhysique.CODEC.optionalFieldOf("extreme_physique", ExtremePhysique.NONE)
                    .forGetter(Aperture::extremePhysique),
            Codec.LONG.optionalFieldOf("current_essence", 0L).forGetter(Aperture::currentEssence),
            ApertureState.CODEC.optionalFieldOf("state", ApertureState.ALIVE).forGetter(Aperture::state),
            GuPath.CODEC.optionalFieldOf("primary_path").forGetter(a -> Optional.ofNullable(a.primaryPath())),
            GuPath.CODEC.optionalFieldOf("secondary_path").forGetter(a -> Optional.ofNullable(a.secondaryPath()))
    ).apply(instance, (rank, stage, base, physique, essence, state, primary, secondary) ->
            new Aperture(rank, stage, base, physique, essence, state,
                    primary.orElse(null), secondary.orElse(null))));

    private static final StreamCodec<ByteBuf, Rank> RANK = ModStreamCodecs.ofEnum(Rank.class);
    private static final StreamCodec<ByteBuf, Stage> STAGE = ModStreamCodecs.ofEnum(Stage.class);
    private static final StreamCodec<ByteBuf, ExtremePhysique> PHYSIQUE =
            ModStreamCodecs.ofEnum(ExtremePhysique.class);
    private static final StreamCodec<ByteBuf, ApertureState> STATE = ModStreamCodecs.ofEnum(ApertureState.class);
    private static final StreamCodec<ByteBuf, GuPath> PATH = ModStreamCodecs.ofNullableEnum(GuPath.class);

    //  ⚠ Hand-written, and it has to be: StreamCodec.composite stops at SIX components and this record
    //  has eight. Field order here must match decode's -- there is no compiler check on that.
    public static final StreamCodec<ByteBuf, Aperture> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull Aperture decode(@NotNull ByteBuf buf) {
            return new Aperture(
                    RANK.decode(buf),
                    STAGE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    PHYSIQUE.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    STATE.decode(buf),
                    PATH.decode(buf),
                    PATH.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull Aperture value) {
            RANK.encode(buf, value.rank());
            STAGE.encode(buf, value.stage());
            ByteBufCodecs.VAR_INT.encode(buf, value.baseEssence());
            PHYSIQUE.encode(buf, value.extremePhysique());
            ByteBufCodecs.VAR_LONG.encode(buf, value.currentEssence());
            STATE.encode(buf, value.state());
            PATH.encode(buf, value.primaryPath());
            PATH.encode(buf, value.secondaryPath());
        }
    };

    //  ⚠ 1..19 is a hole, not a value: no talent tier there, but a live essence cap. Snap out of it.
    public Aperture {
        baseEssence = baseEssence <= 0 ? 0 : Math.clamp(baseEssence, MIN_BASE, MAX_BASE);
        currentEssence = Math.clamp(currentEssence, 0L, maxEssence(rank, stage, baseEssence));
        //  ⚠ 辅修 can never equal 主修 -- enforced here, so the pair is unrepresentable rather than
        //  merely refused. Binding a Vital Gu of the 辅修 path is what makes this fire.
        if (secondaryPath != null && secondaryPath == primaryPath) secondaryPath = null;
    }

    //  Awakening (开窍): Rank I Initial + a rolled tier + its physique, and a full pool.
    //  ⚠ No paths: 主修 comes from a Vital Gu, 辅修 from the player. Awakening grants neither.
    public static Aperture opened() {
        Talent talent = Talent.randomTalent();
        ExtremePhysique physique = talent == Talent.EXTREME
                ? ExtremePhysique.randomTenExtreme()
                : ExtremePhysique.NONE;

        int base = Talent.randomPercent(talent);
        long max = maxEssence(Rank.ONE, Stage.INIT, base);
        return new Aperture(Rank.ONE, Stage.INIT, base, physique, max, ApertureState.ALIVE, null, null);
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

    //  ---- withers ----  eight components: each one is a block, and the tail wraps
    public Aperture withRank(Rank v) {
        return new Aperture(v, stage, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath);
    }
    public Aperture withStage(Stage v) {
        return new Aperture(rank, v, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath);
    }
    public Aperture withBaseEssence(int v) {
        return new Aperture(rank, stage, v, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath);
    }
    public Aperture withExtremePhysique(ExtremePhysique v) {
        return new Aperture(rank, stage, baseEssence, v, currentEssence, state,
                primaryPath, secondaryPath);
    }
    public Aperture withCurrentEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, v, state,
                primaryPath, secondaryPath);
    }
    public Aperture withState(ApertureState v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, v,
                primaryPath, secondaryPath);
    }
    public Aperture withPrimaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, state,
                v, secondaryPath);
    }
    public Aperture withSecondaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, v);
    }
}

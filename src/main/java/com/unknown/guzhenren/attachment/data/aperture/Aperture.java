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

public record Aperture(
        Rank rank,
        Stage stage,
        int baseEssence,
        ExtremePhysique extremePhysique,
        long currentEssence,
        ApertureState state,
        @Nullable GuPath primaryPath,
        @Nullable GuPath secondaryPath,
        long distilledEssence
) {

    public static final int MIN_BASE = 20;
    public static final int MAX_BASE = 100;

    public static final Aperture NONE = new Aperture(
            Rank.NONE, Stage.NONE, 0, ExtremePhysique.NONE, 0L, ApertureState.ALIVE, null, null, 0L);

    public static final Codec<Aperture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rank.CODEC.optionalFieldOf("rank", Rank.NONE).forGetter(Aperture::rank),
            Stage.CODEC.optionalFieldOf("stage", Stage.NONE).forGetter(Aperture::stage),
            Codec.INT.optionalFieldOf("base_essence", 0).forGetter(Aperture::baseEssence),
            ExtremePhysique.CODEC.optionalFieldOf("extreme_physique", ExtremePhysique.NONE)
                    .forGetter(Aperture::extremePhysique),
            Codec.LONG.optionalFieldOf("current_essence", 0L).forGetter(Aperture::currentEssence),
            ApertureState.CODEC.optionalFieldOf("state", ApertureState.ALIVE).forGetter(Aperture::state),
            GuPath.CODEC.optionalFieldOf("primary_path").forGetter(a -> Optional.ofNullable(a.primaryPath())),
            GuPath.CODEC.optionalFieldOf("secondary_path").forGetter(a -> Optional.ofNullable(a.secondaryPath())),
            Codec.LONG.optionalFieldOf("distilled_essence", 0L).forGetter(Aperture::distilledEssence)
    ).apply(instance, (rank, stage, base, physique, essence, state, primary, secondary, distilled) ->
            new Aperture(rank, stage, base, physique, essence, state,
                    primary.orElse(null), secondary.orElse(null), distilled)));

    private static final StreamCodec<ByteBuf, Rank> RANK = ModStreamCodecs.ofEnum(Rank.class);
    private static final StreamCodec<ByteBuf, Stage> STAGE = ModStreamCodecs.ofEnum(Stage.class);
    private static final StreamCodec<ByteBuf, ExtremePhysique> PHYSIQUE =
            ModStreamCodecs.ofEnum(ExtremePhysique.class);
    private static final StreamCodec<ByteBuf, ApertureState> STATE = ModStreamCodecs.ofEnum(ApertureState.class);
    private static final StreamCodec<ByteBuf, GuPath> PATH = ModStreamCodecs.ofNullableEnum(GuPath.class);

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
                    PATH.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf));
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
            ByteBufCodecs.VAR_LONG.encode(buf, value.distilledEssence());
        }
    };

    public Aperture {
        baseEssence = baseEssence <= 0 ? 0 : Math.clamp(baseEssence, MIN_BASE, MAX_BASE);
        currentEssence = Math.clamp(currentEssence, 0L, maxEssence(rank, stage, baseEssence));
        distilledEssence = Math.clamp(distilledEssence, 0L, maxEssence(rank, stage, baseEssence));
        if (secondaryPath != null && secondaryPath == primaryPath) secondaryPath = null;
    }

    public static Aperture opened() {
        Talent talent = Talent.randomTalent();
        ExtremePhysique physique = talent == Talent.EXTREME
                ? ExtremePhysique.randomTenExtreme()
                : ExtremePhysique.NONE;

        int base = Talent.randomPercent(talent);
        long max = maxEssence(Rank.ONE, Stage.INIT, base);
        return new Aperture(Rank.ONE, Stage.INIT, base, physique, max, ApertureState.ALIVE, null, null, 0L);
    }

    public static long maxEssence(Rank rank, Stage stage, int base) {
        return Math.max(0L, (long) base * stage.getEssenceMultiplier() * rank.getRankBase());
    }

    public long maxEssence() {return maxEssence(rank, stage, baseEssence);}
    public Talent talent() {return Talent.fromPercent(baseEssence);}
    public boolean isExtreme() {return talent() == Talent.EXTREME;}
    public boolean isAlive() {return state == ApertureState.ALIVE;}
    public Aperture refilled() {return withCurrentEssence(maxEssence());}

    public Aperture withRank(Rank v) {
        return new Aperture(v, stage, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withStage(Stage v) {
        return new Aperture(rank, v, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withBaseEssence(int v) {
        return new Aperture(rank, stage, v, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withExtremePhysique(ExtremePhysique v) {
        return new Aperture(rank, stage, baseEssence, v, currentEssence, state,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withCurrentEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, v, state,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withState(ApertureState v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, v,
                primaryPath, secondaryPath, distilledEssence);
    }
    public Aperture withPrimaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, state,
                v, secondaryPath, distilledEssence);
    }
    public Aperture withSecondaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, v, distilledEssence);
    }
    public Aperture withDistilledEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence, state,
                primaryPath, secondaryPath, v);
    }
}

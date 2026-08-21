package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

/**
 * One Aperture [空窍]: the vessel a cultivator awakens, and the thing that decides how much essence fits.
 *
 * <p>Leaf record nested inside {@link ApertureData}; immutable. Ten components: rank, stage,
 * baseEssence, extremePhysique, currentEssence, two nullable {@link GuPath} (primary/secondary), and
 * distilledEssence, pressure and the pressure deadline. The compact ctor is the only clamp -- it derives the cap and floors current
 * against it, and makes a secondary path equal to the primary one unrepresentable.
 *
 * <p>⚠ This record has TEN components, so its {@code STREAM_CODEC} is hand-written --
 * {@code StreamCodec.composite} stops at six, and the encode/decode order matches by hand with no
 * compile-time check. ⚠ {@code baseEssence} is clamped to {@code [MIN_BASE, MAX_BASE]} (20..100) when
 * positive; {@code 0} is reserved for {@code NONE} and {@code 1..19} is a hole, not a value. ⚠ The two
 * {@link GuPath} fields are the ONLY nullables in the whole data model, carried via
 * {@code ofNullableEnum} (ordinal+1, 0 = unset). ⚠ {@code openedAt} rolls the physique die, so reading
 * the physique BEFORE {@code ApertureService.enforce} lands sees {@code NONE} and the talent grant
 * silently never happens.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see ApertureData
 * @see ApertureService
 */
public record Aperture(
        Rank rank,
        Stage stage,
        int baseEssence,
        ExtremePhysique extremePhysique,
        long currentEssence,
        @Nullable GuPath primaryPath,
        @Nullable GuPath secondaryPath,
        long distilledEssence,
        int pressure,
        long pressureDeadlineTick
) {

    public static final int MIN_BASE = 20;
    public static final int MAX_BASE = 100;
    public static final int MAX_PRESSURE = 100;
    public static final int PRESSURE_COUNTDOWN_START = MAX_PRESSURE - 1;

    public static final Aperture NONE = new Aperture(
            Rank.NONE, Stage.NONE, 0, ExtremePhysique.NONE, 0L, null, null, 0L, 0, 0L);

    public static final Codec<Aperture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rank.CODEC.optionalFieldOf("rank", Rank.NONE).forGetter(Aperture::rank),
            Stage.CODEC.optionalFieldOf("stage", Stage.NONE).forGetter(Aperture::stage),
            Codec.INT.optionalFieldOf("base_essence", 0).forGetter(Aperture::baseEssence),
            ExtremePhysique.CODEC.optionalFieldOf("extreme_physique", ExtremePhysique.NONE)
                    .forGetter(Aperture::extremePhysique),
            Codec.LONG.optionalFieldOf("current_essence", 0L).forGetter(Aperture::currentEssence),
            GuPath.CODEC.optionalFieldOf("primary_path").forGetter(a -> Optional.ofNullable(a.primaryPath())),
            GuPath.CODEC.optionalFieldOf("secondary_path").forGetter(a -> Optional.ofNullable(a.secondaryPath())),
            Codec.LONG.optionalFieldOf("distilled_essence", 0L).forGetter(Aperture::distilledEssence),
            Codec.INT.optionalFieldOf("pressure", 0).forGetter(Aperture::pressure),
            Codec.LONG.optionalFieldOf("pressure_deadline_tick", 0L).forGetter(Aperture::pressureDeadlineTick)
    ).apply(instance, (rank, stage, base, physique, essence, primary, secondary, distilled, pressure, deadline) ->
            new Aperture(rank, stage, base, physique, essence,
                    primary.orElse(null), secondary.orElse(null), distilled, pressure, deadline)));

    private static final StreamCodec<ByteBuf, Rank> RANK = ModStreamCodecs.ofEnum(Rank.class);
    private static final StreamCodec<ByteBuf, Stage> STAGE = ModStreamCodecs.ofEnum(Stage.class);
    private static final StreamCodec<ByteBuf, ExtremePhysique> PHYSIQUE =
            ModStreamCodecs.ofEnum(ExtremePhysique.class);
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
                    PATH.decode(buf),
                    PATH.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull Aperture value) {
            RANK.encode(buf, value.rank());
            STAGE.encode(buf, value.stage());
            ByteBufCodecs.VAR_INT.encode(buf, value.baseEssence());
            PHYSIQUE.encode(buf, value.extremePhysique());
            ByteBufCodecs.VAR_LONG.encode(buf, value.currentEssence());
            PATH.encode(buf, value.primaryPath());
            PATH.encode(buf, value.secondaryPath());
            ByteBufCodecs.VAR_LONG.encode(buf, value.distilledEssence());
            ByteBufCodecs.VAR_INT.encode(buf, value.pressure());
            ByteBufCodecs.VAR_LONG.encode(buf, value.pressureDeadlineTick());
        }
    };

    public Aperture {
        baseEssence = baseEssence <= 0 ? 0 : Math.clamp(baseEssence, MIN_BASE, MAX_BASE);
        currentEssence = Math.clamp(currentEssence, 0L, maxEssence(rank, stage, baseEssence));
        distilledEssence = Math.clamp(distilledEssence, 0L, maxEssence(rank, stage, baseEssence));
        pressure = extremePhysique == ExtremePhysique.NONE ? 0 : Math.clamp(pressure, 0, MAX_PRESSURE);
        pressureDeadlineTick = pressure == PRESSURE_COUNTDOWN_START ? Math.max(0L, pressureDeadlineTick) : 0L;
        if (secondaryPath != null && secondaryPath == primaryPath) secondaryPath = null;
    }

    public static Aperture opened() {return openedAt(Talent.randomPercent(Talent.randomTalent()));}

    public static Aperture openedAt(int baseEssence) {
        ExtremePhysique physique = Talent.fromPercent(baseEssence) == Talent.EXTREME
                ? ExtremePhysique.randomTenExtreme()
                : ExtremePhysique.NONE;

        long max = maxEssence(Rank.ONE, Stage.INIT, baseEssence);
        return new Aperture(Rank.ONE, Stage.INIT, baseEssence, physique, max, null, null, 0L, 0, 0L);
    }

    public static long maxEssence(Rank rank, Stage stage, int base) {
        return Math.max(0L, (long) base * stage.getEssenceMultiplier() * rank.getRankBase());
    }

    public long maxEssence() {return maxEssence(rank, stage, baseEssence);}
    public Talent talent() {return Talent.fromPercent(baseEssence);}
    public boolean isExtreme() {return talent() == Talent.EXTREME;}
    public Aperture refilled() {return withCurrentEssence(maxEssence());}

    public Aperture withRank(Rank v) {
        return new Aperture(v, stage, baseEssence, extremePhysique, currentEssence,
                primaryPath, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withStage(Stage v) {
        return new Aperture(rank, v, baseEssence, extremePhysique, currentEssence,
                primaryPath, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withBaseEssence(int v) {
        return new Aperture(rank, stage, v, extremePhysique, currentEssence,
                primaryPath, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withExtremePhysique(ExtremePhysique v) {
        return new Aperture(rank, stage, baseEssence, v, currentEssence,
                primaryPath, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withCurrentEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, v,
                primaryPath, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withPrimaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence,
                v, secondaryPath, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withSecondaryPath(@Nullable GuPath v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence,
                primaryPath, v, distilledEssence, pressure, pressureDeadlineTick);
    }
    public Aperture withDistilledEssence(long v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence,
                primaryPath, secondaryPath, v, pressure, pressureDeadlineTick);
    }

    public Aperture withPressure(int v) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence,
                primaryPath, secondaryPath, distilledEssence, v, 0L);
    }

    public Aperture withPressureAndDeadline(int v, long deadline) {
        return new Aperture(rank, stage, baseEssence, extremePhysique, currentEssence,
                primaryPath, secondaryPath, distilledEssence, v, deadline);
    }
}

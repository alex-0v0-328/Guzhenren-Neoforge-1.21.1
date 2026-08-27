package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.compat.EpicFightIntegration;
import com.unknown.guzhenren.custom.enums.aperture.ApertureStatus;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModDamageTypes;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The only writer of the Aperture [空窍] attachment: awakening [开窍], rank [转数], stage [阶段], talent
 * [资质] and paths [流派].
 *
 * <p>Static service over the {@code aperture_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Most writes route through {@code store}, which also fires
 * {@link HealthService#refresh} and {@link EpicFightIntegration#refresh} so derived combat attributes
 * never lag a rank or aptitude change. Pressure writes are the exception because pressure does not
 * affect derived combat attributes. {@code reconcileTalentPaths}
 * is the one place {@code aperture/} writes {@code body/} -- it grants/revokes the ten-extreme talent
 * Dao marks and the human qi.
 *
 * <p>⚠ The physique-and-talent invariant is enforced here ({@code enforce}) rather than in the record,
 * because repairing it rolls a die and a compact constructor has to stay a pure function. ⚠ The
 * "after" physique must be read AFTER {@code enforce} -- reading it before sees {@code NONE} and the
 * talent grant silently never lands. ⚠ {@code awaken} does NOT refuse an awakened holder -- it
 * appends; the caller gates. ⚠ {@code reconcileTalentPaths} is one of the two existing cross-domain
 * grants; a third one is the threshold to extract a coordinator (the {@code TODO(refactor)} below).
 *
 * @author Alex
 * @version 1.0.0
 * @see ApertureData
 * @see EssenceService
 * @since 1.0.0
 */

public final class ApertureService {

    private ApertureService() {}

    public static final int PRIMARY = ApertureData.PRIMARY;
    private static final int PRESSURE_PER_MINUTE = 2;

    public static final long TALENT_MARK_TOTAL = 1000L;
    public static final long TALENT_HUMAN_QI = 100L;

    static long talentMarksPerPath(ExtremePhysique physique) {
        int paths = physique.getTalentPaths().size();
        return paths == 0 ? 0L : TALENT_MARK_TOTAL / paths;
    }

    public static void syncTalentMarks(@NotNull ServerPlayer player) {
        ExtremePhysique physique = aperture(player).extremePhysique();
        long expected = talentMarksPerPath(physique);
        for (GuPath path : physique.getTalentPaths()) {
            if (PathService.mark(player, path, MarkTag.EXTREME_PHYSIQUE) != expected) {
                PathService.setMark(player, path, MarkTag.EXTREME_PHYSIQUE, expected);
            }
        }
    }

    public static @NotNull ApertureData get(@NotNull Player p) {return p.getData(ModAttachments.APERTURE);}
    public static @NotNull Aperture aperture(@NotNull Player p) {return get(p).primary();}
    public static @NotNull Aperture aperture(@NotNull Player p, int i) {return get(p).get(i);}
    public static boolean isAwakened(@NotNull Player p) {return get(p).isAwakened();}

    /**
     * The one derivation of {@link ApertureStatus}: DEAD outranks STONE -- the zombie is the more
     * thorough negative, and stone still has the pressure way back -- and everything else, alive
     * or half-zombie, reads NORMAL.
     */
    public static @NotNull ApertureStatus status(@NotNull Player p) {
        if (BodyService.isZombie(p)) return ApertureStatus.DEAD;
        if (NourishService.isPetrified(p)) return ApertureStatus.STONE;
        return ApertureStatus.NORMAL;
    }
    public static @NotNull Talent talent(@NotNull Player p) {return aperture(p).talent();}
    public static @NotNull Rank rank(@NotNull Player p) {return aperture(p).rank();}
    public static @NotNull Stage stage(@NotNull Player p) {return aperture(p).stage();}

    public static void setRank(@NotNull ServerPlayer p, @NotNull Rank v) {set(p, PRIMARY, aperture(p).withRank(v));}
    public static void setStage(@NotNull ServerPlayer p, @NotNull Stage v) {set(p, PRIMARY, aperture(p).withStage(v));}
    public static void addBaseEssence(@NotNull ServerPlayer p, int d) {setBaseEssence(p, aperture(p).baseEssence() + d);}

    public static void setTalent(@NotNull ServerPlayer p, @NotNull Talent v) {
        setBaseEssence(p, Talent.randomPercent(v));
    }

    public static void setPrimaryPath(@NotNull ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.primaryPath() == v) return;
        set(p, index, aperture.withPrimaryPath(v));
    }

    public static void setSecondaryPath(@NotNull ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.secondaryPath() == v) return;
        set(p, index, aperture.withSecondaryPath(v));
    }

    public static void shiftRank(@NotNull ServerPlayer p, int d) {setRank(p, aperture(p).rank().shift(d));}
    public static void shiftStage(@NotNull ServerPlayer p, int d) {setStage(p, aperture(p).stage().shift(d));}
    public static void shiftTalent(@NotNull ServerPlayer p, int d) {setTalent(p, aperture(p).talent().shift(d));}

    @SuppressWarnings("resource")
    public static void setPressure(@NotNull ServerPlayer player, int index, int value) {
        Aperture current = aperture(player, index);
        if (!current.isExtreme()) return;
        long deadline = value == Aperture.PRESSURE_COUNTDOWN_START ? current.pressureDeadlineTick() : 0L;
        if (value == Aperture.PRESSURE_COUNTDOWN_START && deadline == 0L) {
            deadline = player.level().getGameTime() + Ticks.HALF_MINUTE;
        }
        if (current.pressure() == value && current.pressureDeadlineTick() == deadline) return;
        setPressureState(player, index, value, deadline);
    }

    public static void relievePressure(@NotNull ServerPlayer player, int amount) {
        Aperture current = aperture(player, PRIMARY);
        if (!current.isExtreme()) return;
        setPressure(player, PRIMARY, Math.max(0, current.pressure() - amount));
    }

    @SuppressWarnings("resource")
    public static void tickPressure(@NotNull ServerPlayer player) {
        Aperture aperture = aperture(player, PRIMARY);
        if (!aperture.isExtreme() || aperture.pressure() >= Aperture.MAX_PRESSURE) return;

        if (aperture.pressure() < Aperture.PRESSURE_COUNTDOWN_START) {
            if (player.tickCount % Ticks.MINUTE != 0) return;
            int next = Math.min(Aperture.PRESSURE_COUNTDOWN_START,
                    aperture.pressure() + PRESSURE_PER_MINUTE);
            long deadline = next == Aperture.PRESSURE_COUNTDOWN_START
                    ? player.level().getGameTime() + Ticks.HALF_MINUTE : 0L;
            setPressureState(player, PRIMARY, next, deadline);
            return;
        }

        long deadline = aperture.pressureDeadlineTick();
        if (deadline == 0L) {
            setPressureState(player, PRIMARY, Aperture.PRESSURE_COUNTDOWN_START,
                    player.level().getGameTime() + Ticks.HALF_MINUTE);
            return;
        }
        if (player.level().getGameTime() >= deadline) setPressure(player, PRIMARY, Aperture.MAX_PRESSURE);
    }

    public static boolean pressureFull(@NotNull Player player) {
        Aperture aperture = aperture(player, PRIMARY);
        return aperture.isExtreme() && aperture.pressure() >= Aperture.MAX_PRESSURE;
    }

    @SuppressWarnings("resource")
    public static long pressureRemainingTicks(@NotNull Player player) {
        Aperture aperture = aperture(player, PRIMARY);
        if (!aperture.isExtreme() || aperture.pressure() != Aperture.PRESSURE_COUNTDOWN_START
                || aperture.pressureDeadlineTick() <= 0L) return 0L;
        return Math.max(0L, aperture.pressureDeadlineTick() - player.level().getGameTime());
    }

    @SuppressWarnings("resource")
    public static void detonatePressure(@NotNull ServerPlayer player) {
        Aperture aperture = aperture(player);
        int radius = pressureExplosionRadius(aperture.rank(), aperture.extremePhysique());
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        setPressure(player, PRIMARY, 0);
        DamageSource source = ModDamageTypes.source(player, ModDamageTypes.APERTURE_PRESSURE_EXPLOSION);
        player.level().explode(null, source, null, x, y, z, 0.0F, false, Level.ExplosionInteraction.NONE);
        PressureExplosionTask.start((ServerLevel) player.level(), x, y, z, radius, aperture.extremePhysique());

        DamageSource disaster = ModDamageTypes.source(player, ModDamageTypes.TEN_EXTREME_DISASTER);
        double radiusSquared = radius * (double) radius;
        AABB bounds = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        for (Entity entity : player.level().getEntities(player, bounds, Entity::isAlive)) {
            if (entity.distanceToSqr(x, y, z) <= radiusSquared) entity.hurt(disaster, 10_000.0F);
        }
        if (!player.isDeadOrDying()) player.hurt(source, Float.MAX_VALUE);
    }

    private static int pressureExplosionRadius(Rank rank, ExtremePhysique physique) {
        int base = 16 * (Math.clamp(rank.ordinal(), Rank.LOWEST.ordinal(), Rank.HIGHEST.ordinal()) + 1);
        return physique == ExtremePhysique.GREAT_STRENGTH_TRUE_MARTIAL ? base + 16 : base;
    }

    private static void setPressureState(ServerPlayer player, int index, int value, long deadline) {
        Aperture current = aperture(player, index);
        if (!current.isExtreme() || (current.pressure() == value && current.pressureDeadlineTick() == deadline)) return;
        player.setData(ModAttachments.APERTURE, get(player).with(index, current.withPressureAndDeadline(value, deadline)));
    }

    public static void setBaseEssence(@NotNull ServerPlayer p, int v) {
        set(p, PRIMARY, aperture(p).withBaseEssence(Math.clamp(v, Aperture.MIN_BASE, Aperture.MAX_BASE)));
    }

    public static void setExtremePhysique(@NotNull ServerPlayer player, @NotNull ExtremePhysique physique) {
        Aperture aperture = aperture(player);

        if (physique == ExtremePhysique.NONE) {
            if (aperture.isExtreme()) {
                aperture = aperture.withBaseEssence(Talent.randomPercent(Talent.randomNormalTalent()));
            }
            aperture = aperture.withExtremePhysique(ExtremePhysique.NONE);
        } else {
            aperture = aperture.withBaseEssence(Aperture.MAX_BASE).withExtremePhysique(physique);
        }

        set(player, PRIMARY, aperture);
    }

    public static void awaken(@NotNull ServerPlayer player) {open(player, Aperture.opened());}

    public static void awaken(@NotNull ServerPlayer player, int baseEssence) {
        open(player, Aperture.openedAt(baseEssence));
    }

    private static void open(ServerPlayer player, Aperture aperture) {
        ExtremePhysique before = aperture(player).extremePhysique();
        store(player, get(player).opened(enforce(aperture)));
        reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    public static void set(@NotNull ServerPlayer player, int index, @NotNull Aperture aperture) {
        ExtremePhysique before = index == PRIMARY ? aperture(player).extremePhysique() : null;
        store(player, get(player).with(index, enforce(aperture)));
        if (index == PRIMARY) reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    private static void store(ServerPlayer p, ApertureData data) {
        p.setData(ModAttachments.APERTURE, data);
        HealthService.refresh(p);
        EpicFightIntegration.refresh(p);
    }

    private static Aperture enforce(Aperture aperture) {
        boolean extreme = aperture.isExtreme();
        boolean hasPhysique = aperture.extremePhysique() != ExtremePhysique.NONE;

        if (extreme && !hasPhysique) {
            return aperture.withExtremePhysique(ExtremePhysique.randomTenExtreme());
        }
        if (!extreme && hasPhysique) {
            return aperture.withExtremePhysique(ExtremePhysique.NONE);
        }
        return aperture;
    }

    //    TODO(refactor): extract a coordinator once cross-domain grant rules reach 3; TWO exist today.
    private static void reconcileTalentPaths(ServerPlayer player, ExtremePhysique before, ExtremePhysique after) {
        if (before == after) return;
        grantTalentPaths(player, before, -1);
        grantTalentPaths(player, after, 1);
    }

    private static void grantTalentPaths(ServerPlayer player, ExtremePhysique physique, int sign) {
        List<GuPath> paths = physique.getTalentPaths();
        if (paths.isEmpty()) return;

        QiService.add(player, QiKind.HUMAN, sign * TALENT_HUMAN_QI);
        long marks = sign * talentMarksPerPath(physique);
        for (GuPath path : paths) PathService.addMark(player, path, MarkTag.EXTREME_PHYSIQUE, marks);
    }
}

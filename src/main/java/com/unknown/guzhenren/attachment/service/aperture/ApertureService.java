package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.compat.EpicFightIntegration;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The only writer of the Aperture [空窍] attachment: awakening [开窍], rank, stage, talent and paths.
 *
 * <p>Static service over the {@code aperture_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Most writes route through {@code store}, which also fires
 * {@link HealthService#refresh} and {@link EpicFightIntegration#refresh} so derived combat attributes
 * never lag a rank or aptitude change. Pressure writes are the exception because pressure does not
 * affect derived combat attributes. {@code reconcileTalentPaths}
 * is the one place {@code aperture/} writes {@code body/} -- it grants/revokes the ten-extreme talent
 * specks and the human qi.
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
 * @since 1.0.0
 * @see ApertureData
 * @see EssenceService
 */
public final class ApertureService {

    private ApertureService() {}

    public static final int PRIMARY = ApertureData.PRIMARY;

    public static final long TALENT_SPECK_TOTAL = 5000L;
    public static final long TALENT_HUMAN_QI = 100L;

    public static ApertureData get(Player p) {return p.getData(ModAttachments.APERTURE);}
    public static Aperture aperture(Player p) {return get(p).primary();}
    public static Aperture aperture(Player p, int i) {return get(p).get(i);}
    public static boolean isAwakened(Player p) {return get(p).isAwakened();}
    public static Talent talent(Player p) {return aperture(p).talent();}
    public static Rank rank(Player p) {return aperture(p).rank();}
    public static Stage stage(Player p) {return aperture(p).stage();}

    public static void setRank(ServerPlayer p, Rank v) {set(p, PRIMARY, aperture(p).withRank(v));}
    public static void setStage(ServerPlayer p, Stage v) {set(p, PRIMARY, aperture(p).withStage(v));}
    public static void addBaseEssence(ServerPlayer p, int d) {setBaseEssence(p, aperture(p).baseEssence() + d);}

    public static void setTalent(ServerPlayer p, Talent v) {setBaseEssence(p, Talent.randomPercent(v));}

    public static void setPrimaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.primaryPath() == v) return;
        set(p, index, aperture.withPrimaryPath(v));
    }

    public static void setSecondaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.secondaryPath() == v) return;
        set(p, index, aperture.withSecondaryPath(v));
    }

    public static void shiftRank(ServerPlayer p, int d) {setRank(p, aperture(p).rank().shift(d));}
    public static void shiftStage(ServerPlayer p, int d) {setStage(p, aperture(p).stage().shift(d));}
    public static void shiftTalent(ServerPlayer p, int d) {setTalent(p, aperture(p).talent().shift(d));}

    public static void setPressure(ServerPlayer player, int index, int value) {
        Aperture current = aperture(player, index);
        if (!current.isExtreme()) return;
        long deadline = value == Aperture.PRESSURE_COUNTDOWN_START ? current.pressureDeadlineTick() : 0L;
        if (value == Aperture.PRESSURE_COUNTDOWN_START && deadline == 0L) {
            deadline = player.level().getGameTime() + Ticks.MINUTE;
        }
        if (current.pressure() == value && current.pressureDeadlineTick() == deadline) return;
        setPressureState(player, index, value, deadline);
    }

    public static void tickPressure(ServerPlayer player) {
        Aperture aperture = aperture(player, PRIMARY);
        if (!aperture.isExtreme() || aperture.pressure() >= Aperture.MAX_PRESSURE) return;

        if (aperture.pressure() < Aperture.PRESSURE_COUNTDOWN_START) {
            if (player.tickCount % Ticks.MINUTE != 0) return;
            int next = aperture.pressure() + 1;
            long deadline = next == Aperture.PRESSURE_COUNTDOWN_START
                    ? player.level().getGameTime() + Ticks.MINUTE : 0L;
            setPressureState(player, PRIMARY, next, deadline);
            return;
        }

        long deadline = aperture.pressureDeadlineTick();
        if (deadline == 0L) {
            setPressureState(player, PRIMARY, Aperture.PRESSURE_COUNTDOWN_START,
                    player.level().getGameTime() + Ticks.MINUTE);
            return;
        }
        if (player.level().getGameTime() >= deadline) setPressure(player, PRIMARY, Aperture.MAX_PRESSURE);
    }

    public static boolean pressureFull(Player player) {
        Aperture aperture = aperture(player, PRIMARY);
        return aperture.isExtreme() && aperture.pressure() >= Aperture.MAX_PRESSURE;
    }

    public static long pressureRemainingTicks(Player player) {
        Aperture aperture = aperture(player, PRIMARY);
        if (!aperture.isExtreme() || aperture.pressure() != Aperture.PRESSURE_COUNTDOWN_START
                || aperture.pressureDeadlineTick() <= 0L) return 0L;
        return Math.max(0L, aperture.pressureDeadlineTick() - player.level().getGameTime());
    }

    public static void detonatePressure(ServerPlayer player) {
        setPressure(player, PRIMARY, 0);
        DamageSource source = ModDamageTypes.source(player, ModDamageTypes.APERTURE_PRESSURE_EXPLOSION);

        player.level().explode(null, source, pressureExplosionCalculator(player.getX(), player.getY(), player.getZ()),
                player.getX(), player.getY(), player.getZ(), 64.0F,
                false, Level.ExplosionInteraction.BLOCK);
        if (!player.isDeadOrDying()) player.hurt(source, Float.MAX_VALUE);
    }

    private static ExplosionDamageCalculator pressureExplosionCalculator(double x, double y, double z) {
        Vec3 center = new Vec3(x, y, z);
        return new ExplosionDamageCalculator() {
            private static final double RANGE_SQUARED = 64.0D * 64.0D;

            @Override
            public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state,
                                              float power) {
                return withinRange(explosion, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
                        && super.shouldBlockExplode(explosion, level, pos, state, power);
            }

            @Override
            public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                return withinRange(entity.getX(), entity.getY(), entity.getZ())
                        && super.shouldDamageEntity(explosion, entity);
            }

            @Override
            public float getKnockbackMultiplier(Entity entity) {
                return withinRange(entity.getX(), entity.getY(), entity.getZ())
                        ? super.getKnockbackMultiplier(entity) : 0.0F;
            }

            private boolean withinRange(Explosion explosion, double px, double py, double pz) {
                return withinRange(px, py, pz) && explosion.center().distanceToSqr(px, py, pz) <= RANGE_SQUARED;
            }

            private boolean withinRange(double px, double py, double pz) {
                return center.distanceToSqr(px, py, pz) <= RANGE_SQUARED;
            }
        };
    }

    private static void setPressureState(ServerPlayer player, int index, int value, long deadline) {
        Aperture current = aperture(player, index);
        if (!current.isExtreme() || (current.pressure() == value && current.pressureDeadlineTick() == deadline)) return;
        player.setData(ModAttachments.APERTURE, get(player).with(index, current.withPressureAndDeadline(value, deadline)));
    }

    public static void setBaseEssence(ServerPlayer p, int v) {
        set(p, PRIMARY, aperture(p).withBaseEssence(Math.clamp(v, Aperture.MIN_BASE, Aperture.MAX_BASE)));
    }

    public static void setExtremePhysique(ServerPlayer player, ExtremePhysique physique) {
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

    public static void awaken(ServerPlayer player) {open(player, Aperture.opened());}

    public static void awaken(ServerPlayer player, int baseEssence) {
        open(player, Aperture.openedAt(baseEssence));
    }

    private static void open(ServerPlayer player, Aperture aperture) {
        ExtremePhysique before = aperture(player).extremePhysique();
        store(player, get(player).opened(enforce(aperture)));
        reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    public static void set(ServerPlayer player, int index, Aperture aperture) {
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
        long speck = sign * (TALENT_SPECK_TOTAL / paths.size());
        for (GuPath path : paths) PathService.addSpeck(player, path, MarkTag.NATURAL, speck);
    }
}

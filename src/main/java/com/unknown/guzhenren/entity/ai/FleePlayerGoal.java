package com.unknown.guzhenren.entity.ai;

import com.unknown.guzhenren.entity.BoarGuEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The escape reaction of a boar Gu [豕蛊]: fly away from any player closer than
 * {@link com.unknown.guzhenren.entity.BoarGuEntity#FLEE_RANGE} blocks.
 *
 * <p>Mirrors {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}: a non-creative, non-spectator
 * player is a threat, and the goal only starts when a path to the escape point exists. ⚠ The escape
 * point uses {@link net.minecraft.world.entity.ai.util.AirAndWaterRandomPos} over the same reverse cone
 * that {@code DefaultRandomPos.getPosAway} walks, because the vanilla variant requires a standard
 * block at the target and never fires for a flying mob in open air. The goal holds until the player is
 * farther than {@link com.unknown.guzhenren.entity.BoarGuEntity#ESCAPE_RANGE} blocks. A resting or
 * landing boar takes off before fleeing, so the flight phase never hangs.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.BoarGuEntity
 * @since 1.0.0
 */

public class FleePlayerGoal extends Goal {

    private static final double FLEE_SPEED_MODIFIER = 3.0D;
    private static final int ESCAPE_HORIZONTAL_RANGE = 16;
    private static final int ESCAPE_VERTICAL_RANGE = 7;
    private static final float ESCAPE_CONE_ANGLE = (float) (Math.PI / 2);
    private final BoarGuEntity boar;
    private @Nullable Player threat;
    private @Nullable Vec3 escapeCourse;
    public FleePlayerGoal(BoarGuEntity boar) {
        this.boar = boar;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }
    @Override
    public boolean canUse() {
        threat = nearestThreat(boar);
        if (threat == null) return false;
        escapeCourse = courseAwayFromThreat(threat);
        if (escapeCourse == null) return false;
        boolean courseCloserToThreat = threat.distanceToSqr(escapeCourse.x, escapeCourse.y, escapeCourse.z)
                < threat.distanceToSqr(boar);
        if (courseCloserToThreat) return false;
        return boar.getNavigation().createPath(escapeCourse.x, escapeCourse.y, escapeCourse.z, 0) != null;
    }
    @Override
    public boolean canContinueToUse() {
        return threat != null && threat.isAlive() && boar.distanceTo(threat) < BoarGuEntity.ESCAPE_RANGE;
    }
    @Override
    public void start() {
        if (boar.phase() != BoarGuEntity.FlightPhase.FLYING) boar.takeOff();
        if (escapeCourse != null) {
            boar.getNavigation().moveTo(escapeCourse.x, escapeCourse.y, escapeCourse.z, FLEE_SPEED_MODIFIER);
        }
    }
    @Override
    public void stop() {
        threat = null;
        escapeCourse = null;
    }
    @Override
    public boolean requiresUpdateEveryTick() {return true;}
    @Override
    public void tick() {
        if (threat == null || !boar.getNavigation().isDone()) return;
        Vec3 course = courseAwayFromThreat(threat);
        if (course != null) boar.getNavigation().moveTo(course.x, course.y, course.z, FLEE_SPEED_MODIFIER);
    }
    private @Nullable Vec3 courseAwayFromThreat(@NotNull Player threat) {
        Vec3 away = boar.position().subtract(threat.position());
        return AirAndWaterRandomPos.getPos(boar, ESCAPE_HORIZONTAL_RANGE, ESCAPE_VERTICAL_RANGE, 0,
                away.x, away.z, ESCAPE_CONE_ANGLE);
    }
    @SuppressWarnings("resource")
    static @Nullable Player nearestThreat(BoarGuEntity boar) {
        return boar.level().getNearestPlayer(boar.getX(), boar.getY(), boar.getZ(), BoarGuEntity.FLEE_RANGE,
                FleePlayerGoal::isThreat);
    }
    private static boolean isThreat(Entity entity) {
        return entity instanceof Player player && !player.isSpectator() && !player.isCreative();
    }
}

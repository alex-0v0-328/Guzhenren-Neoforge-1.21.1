package com.unknown.guzhenren.entity.ai;

import com.unknown.guzhenren.entity.BoarGuEntity;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * The ground rest of a boar Gu [豕蛊]: land in its own column, rest, then take off again.
 *
 * <p>Starts only when the boar requested a landing and no player stands within
 * {@link com.unknown.guzhenren.entity.BoarGuEntity#FLEE_RANGE} blocks. The landing spot is the standable
 * position at {@code level.getHeight(MOTION_BLOCKING, x, z)} in the boar's own column. Arrival starts a
 * rest of 8 to 10 seconds with a cleared motion vector and occasional head turns; the timer running out
 * takes the boar back into flight. A landing that stalls or cannot be pathed aborts via
 * {@code takeOff} so the goal never wedges.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.BoarGuEntity
 * @since 1.0.0
 */

public class LandRestGoal extends Goal {

    private static final double LAND_SPEED_MODIFIER = 1.0D;
    private static final double ARRIVAL_RANGE = 1.5D;
    private static final int REST_TICKS = 160;
    private static final int REST_JITTER_TICKS = 41;
    private static final int LOOK_AROUND_ROLL = 80;
    private static final int LOOK_YAW_SPREAD = 181;
    private static final int LOOK_YAW_CENTER = 90;
    private static final int LANDING_TIMEOUT_TICKS = 600;

    private final BoarGuEntity boar;
    private Vec3 landingSpot = Vec3.ZERO;
    private int restRemaining;
    private int landingTicks;

    public LandRestGoal(BoarGuEntity boar) {
        this.boar = boar;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }
    @Override
    public boolean canUse() {return boar.wantsToLand() && FleePlayerGoal.nearestThreat(boar) == null;}
    @Override
    public boolean canContinueToUse() {
        return boar.phase() != BoarGuEntity.FlightPhase.FLYING && FleePlayerGoal.nearestThreat(boar) == null;
    }
    @Override
    public void start() {
        boar.beginLanding();
        restRemaining = 0;
        landingTicks = 0;
        retargetGround();
    }
    @Override
    public void stop() {
        restRemaining = 0;
        boar.getNavigation().stop();
    }
    @Override
    public boolean requiresUpdateEveryTick() {return true;}
    @Override
    public void tick() {
        if (boar.phase() == BoarGuEntity.FlightPhase.LANDING) tickLanding();
        else tickResting();
    }
    private void tickLanding() {
        landingTicks++;
        if (boar.onGround() || boar.position().distanceTo(landingSpot) < ARRIVAL_RANGE) {
            boar.getNavigation().stop();
            boar.beginResting();
            restRemaining = REST_TICKS + boar.getRandom().nextInt(REST_JITTER_TICKS);
            boar.setDeltaMovement(Vec3.ZERO);
        } else if (landingTicks >= LANDING_TIMEOUT_TICKS) {
            boar.takeOff();
        } else if (boar.getNavigation().isDone() && !retargetGround()) {
            boar.takeOff();
        }
    }
    private void tickResting() {
        boar.setDeltaMovement(Vec3.ZERO);
        if (--restRemaining <= 0) {
            boar.takeOff();
            return;
        }
        if (boar.getRandom().nextInt(LOOK_AROUND_ROLL) == 0) {
            boar.setYRot(boar.getYRot() + boar.getRandom().nextInt(LOOK_YAW_SPREAD) - LOOK_YAW_CENTER);
        }
    }
    private boolean retargetGround() {
        int x = Mth.floor(boar.getX());
        int z = Mth.floor(boar.getZ());
        int y = boar.level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        landingSpot = new Vec3(x + 0.5D, y, z + 0.5D);
        return boar.getNavigation().moveTo(landingSpot.x, landingSpot.y, landingSpot.z, LAND_SPEED_MODIFIER);
    }
}

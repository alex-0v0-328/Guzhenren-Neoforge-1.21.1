package com.unknown.guzhenren.entity.ai;

import com.unknown.guzhenren.entity.BoarGuEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.phys.Vec3;

/**
 * The wander course of a boar Gu [豕蛊]: continuous random flight with a forced course change.
 *
 * <p>Extends {@link net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal} so the cone-based
 * point picking stays vanilla. A finished path is re-selected on the very next tick, and every
 * {@link #RECOURSE_TICKS} ticks the course is re-picked mid-flight. Each re-selection rolls a
 * one-in-ten chance to request a landing, handing control to
 * {@link com.unknown.guzhenren.entity.ai.LandRestGoal}. ⚠ {@code canUse} force-triggers past the
 * vanilla {@code noActionTime >= 100} gate, which would otherwise permanently reject the goal.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.BoarGuEntity
 * @since 1.0.0
 */

public class WanderCourseGoal extends WaterAvoidingRandomFlyingGoal {

    private static final double SPEED_MODIFIER = 1.0D;
    private static final int RECOURSE_TICKS = 100;
    private static final int LANDING_ROLL_SIDES = 10;
    private final BoarGuEntity boar;
    private int courseTicks;
    public WanderCourseGoal(BoarGuEntity boar) {
        super(boar, SPEED_MODIFIER);
        this.boar = boar;
    }
    @Override
    public boolean canUse() {
        trigger();
        if (!super.canUse()) return false;
        rollLanding();
        return true;
    }
    @Override
    public void start() {
        super.start();
        courseTicks = 0;
    }
    @Override
    public boolean requiresUpdateEveryTick() {return true;}
    @Override
    public void tick() {
        courseTicks++;
        if (courseTicks < RECOURSE_TICKS) return;
        courseTicks = 0;
        Vec3 course = getPosition();
        if (course == null) return;
        boar.getNavigation().moveTo(course.x, course.y, course.z, speedModifier);
        rollLanding();
    }
    private void rollLanding() {
        if (boar.getRandom().nextInt(LANDING_ROLL_SIDES) == 0) boar.requestLanding();
    }
}

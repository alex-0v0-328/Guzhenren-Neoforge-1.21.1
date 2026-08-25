package com.unknown.guzhenren.entity;

import com.unknown.guzhenren.entity.ai.HoverNearPlayerGoal;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wild Gu that flies: it drifts toward a player it wants, and hovers there.
 *
 * <p>Extends {@link com.unknown.guzhenren.entity.WildGuEntity}. Uses a flying move control and flying
 * path navigation; the {@code HoverNearPlayerGoal} drives the movement vector directly. Spawns
 * full bright {@code END_ROD} particles at one per tick. The {@code seeks} method is the one door for
 * "who does it fly toward"; the base wants anyone, a leaf narrows it.
 *
 * <p>⚠ {@code isNoGravity()} is a flat true on purpose. The flying move control only clears gravity
 * while it is actively moving the mob, and the hover goal stops the navigation.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.WildGuEntity
 * @see com.unknown.guzhenren.entity.ai.HoverNearPlayerGoal
 * @since 1.0.0
 */

@SuppressWarnings("resource")
public class FlyingGuEntity extends WildGuEntity {

    public static final double DETECT_RANGE = 12.0;
    public static final double HOVER_RANGE = 2.0;

    private static final double FOLLOW_RANGE = 16.0;
    private static final double MAX_HEALTH = 1.0;
    private static final double FLYING_SPEED = 0.1;
    private static final double MOVEMENT_SPEED = 0.1;
    private static final double WANDER_SPEED = 1.0;

    private static final int MOTES_PER_TICK = 1;
    private static final double MOTE_SPREAD = 0.25;

    private static final int TURN_RATE = 20;

    private final ParticleOptions motes;

    public FlyingGuEntity(EntityType<? extends FlyingGuEntity> type, Level level,
                          Supplier<Item> caughtGu, ParticleOptions motes) {
        super(type, level, caughtGu);
        this.moveControl = new FlyingMoveControl(this, TURN_RATE, true);
        this.motes = motes;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new HoverNearPlayerGoal(this));
        goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, WANDER_SPEED));
    }

    //region who it flies toward -- the base wants anyone, a leaf narrows it
    public boolean seeks(Player player) {return true;}

    public @Nullable Player seekTarget() {
        return level().getNearestPlayer(getX(), getY(), getZ(), DETECT_RANGE, this::wanted);
    }

    private boolean wanted(Entity entity) {
        return entity instanceof Player player && !player.isSpectator() && seeks(player);
    }
    //endregion

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    public boolean isNoGravity() {return true;}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) spawnMotes();
    }

    private void spawnMotes() {
        for (int i = 0; i < MOTES_PER_TICK; i++) {
            level().addParticle(motes, getRandomX(MOTE_SPREAD), getRandomY(), getRandomZ(MOTE_SPREAD),
                    0.0, 0.0, 0.0);
        }
    }
}

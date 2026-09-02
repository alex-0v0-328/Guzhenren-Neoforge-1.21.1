package com.unknown.guzhenren.entity;

import com.unknown.guzhenren.entity.ai.FleePlayerGoal;
import com.unknown.guzhenren.entity.ai.LandRestGoal;
import com.unknown.guzhenren.entity.ai.WanderCourseGoal;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A wild boar Gu [野生豕蛊] that wanders freely instead of seeking players.
 *
 * <p>Behavior: random flight with a course change every five seconds, occasional ground rests of
 * eight to ten seconds, and a triple-speed flight away from any player closer than
 * {@link #FLEE_RANGE} blocks. Animation runs on GeckoLib [GeckoLib]: {@code fly} loops while
 * airborne, {@code land} and {@code takeoff} are server-triggered one-shots tied to the flight
 * phase.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.FlyingGuEntity
 * @since 1.0.0
 */

public class BoarGuEntity extends FlyingGuEntity implements GeoEntity {

    public static final double FLEE_RANGE = 6.0;
    public static final double ESCAPE_RANGE = 10.0;

    //region GeckoLib animation -- one controller, takeoff/land are server-triggered one-shots
    private static final RawAnimation FLY_ANIM =
            RawAnimation.begin().thenLoop("animation.boar_gu.fly");
    private static final RawAnimation LAND_ANIM =
            RawAnimation.begin().thenPlayAndHold("animation.boar_gu.land");
    private static final RawAnimation TAKEOFF_ANIM = RawAnimation.begin()
            .thenPlay("animation.boar_gu.takeoff").thenLoop("animation.boar_gu.fly");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    //endregion

    public BoarGuEntity(EntityType<? extends BoarGuEntity> type, Level level, Supplier<Item> caughtGu) {
        super(type, level, caughtGu);
    }
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FleePlayerGoal(this));
        goalSelector.addGoal(1, new LandRestGoal(this));
        goalSelector.addGoal(2, new WanderCourseGoal(this));
    }
    @Override
    public boolean seeks(Player player) {return false;}

    //region flight phase -- server authoritative; airborne animation follows onGround, one-shots carry the rest
    public enum FlightPhase {FLYING, LANDING, RESTING}

    private FlightPhase phase = FlightPhase.FLYING;
    private boolean wantsToLand;

    public FlightPhase phase() {return phase;}
    public boolean wantsToLand() {return wantsToLand;}
    public void requestLanding() {wantsToLand = true;}
    public void beginLanding() {phase = FlightPhase.LANDING; triggerAnim("main", "land");}
    public void beginResting() {phase = FlightPhase.RESTING; wantsToLand = false;}
    public void takeOff() {phase = FlightPhase.FLYING; wantsToLand = false; triggerAnim("main", "takeoff");}
    //endregion

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (!onGround())
                return state.setAndContinue(FLY_ANIM);
            return PlayState.STOP;
        }).triggerableAnim("land", LAND_ANIM).triggerableAnim("takeoff", TAKEOFF_ANIM));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return cache;}
}

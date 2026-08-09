package com.unknown.guzhenren.entity;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class HopeGuEntity extends FlyingGuEntity {

    private static final int  FADE_TICKS  = Ticks.MINUTE;
    private static final long NOT_SIGHTED = -1L;

    private long fadeAtTick = NOT_SIGHTED;

    public HopeGuEntity(EntityType<? extends HopeGuEntity> type, Level level,
                        Supplier<Item> caughtGu, ParticleOptions motes) {
        super(type, level, caughtGu, motes);
    }

    //    ⚠⚠ 开窍 [awakening] is the only thing this Gu grants, so an awakened player has nothing to gain and
    //    it drifts past them. ⚠ Only the APPROACH reads this -- the catch itself stays ungated, always.
    @Override
    public boolean seeks(Player player) {return !ApertureService.isAwakened(player);}

    //    ⚠⚠ ANY player opens the window, not only one it seeks. Were it the sought player alone, a
    //    cultivator would never close it and these would pile up to the category cap around the one
    //    person who has no use for them.
    //    ⚠⚠ It opens on the FIRST sighting and never reopens: walking away neither resets nor pauses it.
    //    ⚠ Deliberately unserialized -- a reloaded Gu starts its minute over, and these despawn anyway.
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (fadeAtTick == NOT_SIGHTED) {
            if (level().getNearestPlayer(this, DETECT_RANGE) != null) {
                fadeAtTick = level().getGameTime() + FADE_TICKS;
            }
            return;
        }
        if (level().getGameTime() >= fadeAtTick) discard();
    }
}

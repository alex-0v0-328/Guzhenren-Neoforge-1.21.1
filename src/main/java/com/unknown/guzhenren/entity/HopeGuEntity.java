package com.unknown.guzhenren.entity;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The wild Hope Gu [希望蛊]: the world's own source of an awakening [开窍].
 *
 * <p>Extends {@link com.unknown.guzhenren.entity.FlyingGuEntity}. Overrides {@code seeks} to target
 * only the unawakened -- a cultivator drifts past. Once any player comes within {@code DETECT_RANGE},
 * a one-minute fade clock starts; the window never reopens, and walking away neither resets nor pauses
 * it. The fade tick is unserialized on purpose -- a reloaded Gu starts over, and these despawn anyway.
 *
 * <p>⚠ It fades once any player has come into range, not once a particular one has. That sighting
 * starts a clock, and nothing restarts it afterward.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.entity.FlyingGuEntity
 * @since 1.0.0
 */

@SuppressWarnings("resource")
public class HopeGuEntity extends FlyingGuEntity {

    private static final int FADE_TICKS = Ticks.MINUTE;
    private static final long NOT_SIGHTED = -1L;

    private long fadeAtTick = NOT_SIGHTED;
    public HopeGuEntity(EntityType<? extends HopeGuEntity> type, Level level,
                        Supplier<Item> caughtGu, ParticleOptions motes) {
        super(type, level, caughtGu, motes);
    }
    @Override
    public boolean seeks(Player player) {return !ApertureService.isAwakened(player);}
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

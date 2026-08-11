package com.unknown.guzhenren.effect.pool;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Half-Zombie [半生半僵]: a projection of the form stored on the body, never a truth of its own.
 *
 * <p>⚠⚠ The heartbeat re-applies and removes it unconditionally, which is what stops a clear command
 * or a death from stranding a player wearing a form they are no longer in.
 *
 * @author Alex
 * @since 1.0.0
 */
public class HalfZombieEffect extends MobEffect {

    public HalfZombieEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}

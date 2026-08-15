package com.unknown.guzhenren.effect.pool;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Half-Zombie [半生半僵] effect — a pool projection of the form stored on {@link
 * com.unknown.guzhenren.attachment.data.body.BodyData}, never a truth of its own.
 *
 * <p>Pool effects are rebuilt every heartbeat by {@code QiService.syncEffects}, so milk, {@code
 * /effect clear} and death cannot strand a player wearing a form they are no longer in — the next
 * tick re-applies or removes it unconditionally.
 *
 * <p>☠ The class body is empty on purpose: the form is the truth, the effect is only the vanilla
 * icon the HUD needs to display it.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.BodyService
 */
public class HalfZombieEffect extends MobEffect {

    public HalfZombieEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}

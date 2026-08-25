package com.unknown.guzhenren.client;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * Draws an effect with the icon of the item that grants it, so the two always read as the same thing.
 *
 * <p>Implements {@link com.unknown.guzhenren.client.EffectIconLayout} as a record carrying only the
 * item's registration id. The texture path is always {@code item/<id>}, ignoring the amplifier -- a
 * non-graded effect has one face.
 *
 * <p>⚠ It takes the item's registration id, not a mob_effect texture. An effect wearing its Gu's face
 * needs no second drawing, and a missing one here would be a checkerboard rather than a fallback.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.client.EffectIconLayout
 * @see com.unknown.guzhenren.client.GradedEffectIcon
 * @since 1.0.0
 */

public record ItemEffectIcon(String item) implements EffectIconLayout {

    @Override
    public String textureFor(MobEffectInstance instance) {
        return "item/" + item;
    }
}

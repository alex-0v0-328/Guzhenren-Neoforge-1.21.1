package com.unknown.guzhenren.effect.pool;

import com.unknown.guzhenren.client.GradedEffectIcon;
import com.unknown.guzhenren.effect.AttackContributor;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Strength Qi [力气] effect — a pool projection of the 力气 held in {@link
 * com.unknown.guzhenren.attachment.data.body.QiData}, which adds attack damage while held.
 *
 * <p>Pool effects are rebuilt every heartbeat by {@code QiService.syncEffects}, so milk cannot cure
 * them. It contributes through {@link com.unknown.guzhenren.effect.AttackContributor} rather than
 * an {@link net.minecraft.world.entity.ai.attributes.AttributeModifier}, so what the body panel
 * shows and what a hit actually deals stay one number.
 *
 * <p>⚠ The {@code ATTACK_BONUS} ladder {0.25, 1, 4, 16, 64} must be exactly representable as a
 * {@code double} — power-of-two denominators only, no float dust.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.effect.AttackContributor
 * @since 1.0.0
 */

public class StrengthQiEffect extends MobEffect implements AttackContributor {

    private static final double[] ATTACK_BONUS = {0.25, 1.0, 4.0, 16.0, 64.0};

    public StrengthQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public double attackBonus(int amplifier) {
        return ATTACK_BONUS[Math.clamp(amplifier, 0, ATTACK_BONUS.length - 1)];
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(GradedEffectIcon.mobEffect("strength_qi", 1, 5));
    }
}

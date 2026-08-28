package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.registry.effect.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The timed buff of the Dragonpill Cricket Gu [龙丸蛐蛐蛊]: a jump-strength lift for thirty seconds,
 * followed by a twenty-second weakness aftermath.
 *
 * <p>Timed effects own their truth on vanilla's timer. This one carries a real {@link
 * net.minecraft.world.entity.ai.attributes.AttributeModifier} on {@link
 * net.minecraft.world.entity.ai.attributes.Attributes#JUMP_STRENGTH} — the one effect that does NOT
 * implement {@link com.unknown.guzhenren.effect.AttackContributor}, because it touches jump, not
 * attack.
 *
 * <p>⚠ The aftermath is applied on the effect's last tick ({@code duration == 1}), because a
 * {@link net.minecraft.world.effect.MobEffect} has no expiry hook — milk or {@code /effect clear}
 * skips the penalty, which is the intended design.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.effect.AttackContributor
 * @since 1.0.0
 */

public class DragonpillCricketGuEffect extends MobEffect {

    public static final int DURATION_TICKS = 30 * Ticks.SECOND;
    public static final int AFTERMATH_TICKS = 20 * Ticks.SECOND;

    public static final double JUMP_BONUS = 0.20D;

    private static final int LAST_TICK = 1;

    private static final ResourceLocation MODIFIER_ID =
            Guzhenren.id("dragonpill_cricket_jump_strength");

    public DragonpillCricketGuEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.JUMP_STRENGTH, MODIFIER_ID, JUMP_BONUS,
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {return duration == LAST_TICK;}

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.addEffect(ModEffects.instance(MobEffects.WEAKNESS, AFTERMATH_TICKS));
        return true;
    }
}

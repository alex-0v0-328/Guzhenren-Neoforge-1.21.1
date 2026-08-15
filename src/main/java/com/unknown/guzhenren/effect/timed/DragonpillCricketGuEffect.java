package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The timed buff of the Dragonpill Cricket Gu [龙丸蛐蛐蛊], which charges on the way out.
 *
 * <p>⚠ Its lift is a real AttributeModifier, so it must be taken off again on the last tick; the
 * aftermath and the removal are the same moment and cannot be separated.
 *
 * @author Alex
 * @since 1.0.0
 */
public class DragonpillCricketGuEffect extends MobEffect {

    public static final int DURATION_TICKS = 30 * Ticks.SECOND;
    public static final int AFTERMATH_TICKS = 20 * Ticks.SECOND;

    public static final double JUMP_BONUS = 0.20D;

    private static final int LAST_TICK = 1;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "dragonpill_cricket_jump_strength");

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

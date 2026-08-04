package com.unknown.guzhenren.effect;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, AFTERMATH_TICKS));
        return true;
    }
}

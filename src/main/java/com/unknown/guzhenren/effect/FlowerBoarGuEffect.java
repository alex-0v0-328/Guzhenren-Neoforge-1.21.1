package com.unknown.guzhenren.effect;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FlowerBoarGuEffect extends MobEffect {

    public static final int DURATION_TICKS = 10 * Ticks.SECOND;

    public static final double ATTACK_BONUS = 4.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "flower_boar_attack_damage");

    public FlowerBoarGuEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER_ID, ATTACK_BONUS,
                AttributeModifier.Operation.ADD_VALUE);
    }
}

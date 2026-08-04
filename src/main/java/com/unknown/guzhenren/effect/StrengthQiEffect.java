package com.unknown.guzhenren.effect;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public class StrengthQiEffect extends MobEffect {

    private static final double[] ATTACK_BONUS = {0.0625, 0.25, 1.0, 4.0, 16.0};

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "strength_qi_attack_damage");

    public StrengthQiEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER_ID, AttributeModifier.Operation.ADD_VALUE,
                amplifier -> bonus(amplifier));
    }

    public static double bonus(int amplifier) {
        return ATTACK_BONUS[Math.clamp(amplifier, 0, ATTACK_BONUS.length - 1)];
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new GradedEffectIcon("strength_qi", 1, 5));
    }
}

package com.unknown.guzhenren.effect.pool;

import com.unknown.guzhenren.client.GradedEffectIcon;
import com.unknown.guzhenren.effect.AttackContributor;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

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
        consumer.accept(new GradedEffectIcon("strength_qi", 1, 5));
    }
}

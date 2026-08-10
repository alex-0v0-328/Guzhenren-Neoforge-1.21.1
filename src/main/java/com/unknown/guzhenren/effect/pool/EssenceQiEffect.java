package com.unknown.guzhenren.effect.pool;

import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public class EssenceQiEffect extends MobEffect {

    private static final double[] REGEN_BONUS = {0.20, 0.40, 0.60, 0.80, 1.00};

    public EssenceQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static double bonus(int amplifier) {
        return REGEN_BONUS[Math.clamp(amplifier, 0, REGEN_BONUS.length - 1)];
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new GradedEffectIcon("essence_qi", 1, 5));
    }
}

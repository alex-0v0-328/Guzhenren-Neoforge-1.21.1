package com.unknown.guzhenren.effect.pool;

import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraft.world.entity.LivingEntity;

public class LifeQiEffect extends MobEffect {

    public static final int HEAL_INTERVAL_TICKS = 10;

    public LifeQiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % HEAL_INTERVAL_TICKS == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.heal(amplifier + 1.0F);
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new GradedEffectIcon("life_qi", 1, 5));
    }
}

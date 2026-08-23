package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public final class SelfRelianceGuEffect extends MobEffect {

    public SelfRelianceGuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Ticks.SECOND == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        int tier = Math.clamp(amplifier - 1, 0, 2);
        float limit = entity.getMaxHealth() * (0.5F + tier * 0.1F);
        if (entity.getHealth() < limit) entity.heal(Math.min(tier + 1.0F, limit - entity.getHealth()));
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(GradedEffectIcon.item("self_reliance_gu", 2, 4));
    }
}

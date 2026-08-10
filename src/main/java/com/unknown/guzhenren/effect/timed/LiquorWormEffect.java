package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public class LiquorWormEffect extends MobEffect {

    public LiquorWormEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new GradedEffectIcon("liquor_worm", 1, 4));
    }
}

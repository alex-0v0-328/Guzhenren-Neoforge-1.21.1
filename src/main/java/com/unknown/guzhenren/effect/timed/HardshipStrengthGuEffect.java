package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.client.ItemEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public final class HardshipStrengthGuEffect extends MobEffect {

    public static final int DURATION_TICKS = 120 * Ticks.SECOND;

    public HardshipStrengthGuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new ItemEffectIcon("hardship_strength_gu"));
    }
}

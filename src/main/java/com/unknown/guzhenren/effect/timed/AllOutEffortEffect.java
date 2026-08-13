package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.client.GradedEffectIcon;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * All-Out Effort [全力以赴]: while it runs, the body's carrying limit [承受上限] does not apply.
 *
 * <p>⚠ A marker with no AttributeModifier. The strength service reads it, so attack still comes out
 * of one formula rather than gaining a second source.
 *
 * @author Alex
 * @since 1.0.0
 */
public class AllOutEffortEffect extends MobEffect {

    public AllOutEffortEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(GradedEffectIcon.mobEffect("all_out_effort", 3, 5));
    }
}

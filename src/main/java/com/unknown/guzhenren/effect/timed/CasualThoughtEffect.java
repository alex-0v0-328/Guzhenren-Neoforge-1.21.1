package com.unknown.guzhenren.effect.timed;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.client.GradedEffectIcon;
import com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * The Casual Gu [随意蛊] effect: each second for ten seconds it adds a random amount of natural
 * thoughts [念] to the mind ocean [脑海].
 *
 * <p>Timed effects own their truth on vanilla's timer. One effect, two grades — the amplifier is
 * the rank's tier, and the {@code RANGES} table widens with it. Thoughts land through {@link
 * com.unknown.guzhenren.attachment.service.mind.MindService#addThoughts} tagged {@code NATURAL}.
 *
 * <p>⚠ {@code applyEffectTick} must {@code return true} — returning false lets vanilla remove the
 * effect on the spot.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.mind.MindService
 */
public class CasualThoughtEffect extends MobEffect {

    public static final int DURATION_TICKS = 10 * Ticks.SECOND;

    private static final int[][] RANGES = {{1, 100}, {100, 200}};

    public CasualThoughtEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Ticks.SECOND == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            int index = Math.clamp(amplifier, 0, RANGES.length - 1);
            int amount = player.getRandom().nextIntBetweenInclusive(RANGES[index][0], RANGES[index][1]);
            MindService.addThoughts(player, amount, ThoughtTag.NATURAL);
        }
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(GradedEffectIcon.item("casual_gu", 1, 2));
    }
}

package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.StaminaService;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Stamina's [耐力] own tick handler: it counts half-seconds itself and bills the jump.
 *
 * <p>Cannot live on {@link PlayerTickEvents}, which returns early unless {@code tickCount % 20 == 0};
 * anything finer than a second must count its own ticks. Every tick it samples {@link
 * net.minecraft.world.food.FoodData}'s exhaustion against the {@code EXHAUSTION_SEEN} scratch array
 * and, while the player is weary, adds half of each rise back. The jump cost lands on {@link
 * net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent}, which is not cancellable,
 * so the cost floors at 0.
 *
 * <p>⚠ {@code EXHAUSTION_SEEN} is anchored in {@code onJoin} — exhaustion is serialized, so an
 * unanchored first sample bills the whole carried-over amount at once.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.StaminaService
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class StaminaEvents {

    private StaminaEvents() {}

    public static final float WEARY_EXTRA_EXHAUSTION = 0.5F;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        chargeWearyExhaustion(player);
        if (player.tickCount % Ticks.HALF_SECOND != 0) return;

        StaminaService.step(player);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        StaminaService.spendOnJump(player);
    }

    private static void chargeWearyExhaustion(ServerPlayer player) {
        float[] seen = player.getData(ModAttachments.EXHAUSTION_SEEN);
        FoodData food = player.getFoodData();
        float now = food.getExhaustionLevel();
        float spent = now - seen[0];
        seen[0] = now;

        if (spent <= 0.0F) return;
        if (!BodyService.lifeForm(player).spendsStamina()) return;
        if (!StaminaService.isWeary(player)) return;

        food.addExhaustion(spent * WEARY_EXTRA_EXHAUSTION);
        seen[0] = food.getExhaustionLevel();
    }
}

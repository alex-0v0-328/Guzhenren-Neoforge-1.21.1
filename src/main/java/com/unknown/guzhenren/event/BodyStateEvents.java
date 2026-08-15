package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * The vanilla moments body [肉身] state answers: breathing, and any add/remove/expire of a {@link
 * net.minecraft.world.effect.MobEffect}.
 *
 * <p>{@code onBreathe} lets an undead life form breathe underwater — {@link
 * com.unknown.guzhenren.attachment.service.body.BodyService#lifeForm} decides who. The three
 * {@link net.neoforged.neoforge.event.entity.living.MobEffectEvent} hooks each call {@link
 * com.unknown.guzhenren.attachment.service.body.AttackService#refresh} so the attack total
 * recomputes the instant an effect arrives or leaves.
 *
 * <p>⚠ Left to the heartbeat alone the attack row would lag by up to a second, which is visible on
 * the panel.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.AttackService
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class BodyStateEvents {

    private BodyStateEvents() {}

    @SubscribeEvent
    public static void onBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (BodyService.lifeForm(player).breathes()) return;

        event.setCanBreathe(true);
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {refreshAttack(event.getEntity());}

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {refreshAttack(event.getEntity());}

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {refreshAttack(event.getEntity());}

    private static void refreshAttack(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) AttackService.refresh(player);
    }
}

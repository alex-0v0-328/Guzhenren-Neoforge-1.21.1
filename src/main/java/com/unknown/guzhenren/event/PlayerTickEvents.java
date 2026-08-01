package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageTick;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.compat.customplayer.PartStorageTick;
import com.unknown.guzhenren.effect.DeathQiEffect;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import com.unknown.guzhenren.registry.ModDamageTypes;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        long days = BodyService.tickAging(player);
        RefinableGuItem.starveAll(player, days);
        ApertureStorageTick.tickDay(player, days);
        PartStorageTick.tickDay(player, days);

        if (days > 0L && player.containerMenu instanceof ApertureStorageMenu menu) menu.reload();

        closeDistilling(player);
        tickDeathQi(player);
        EssenceService.regenStep(player);
        MindService.regenStep(player);
        checkLethalState(player);
    }

    private static void tickDeathQi(ServerPlayer player) {
        if (!player.hasEffect(ModEffects.DEATH_QI)) return;

        if (player.tickCount % DeathQiEffect.YEAR_INTERVAL_TICKS == 0) {
            BodyService.drainByDeathQi(player, DeathQiEffect.YEARS_PER_INTERVAL);
        }
        if (player.getHealth() > DeathQiEffect.HEALTH_FLOOR) {
            player.setHealth(Math.max(DeathQiEffect.HEALTH_FLOOR,
                    player.getHealth() - DeathQiEffect.HEALTH_PER_HEARTBEAT));
        }
    }

    private static void closeDistilling(ServerPlayer player) {
        if (EssenceService.distilledEssence(player) > 0L && !EssenceService.isDistilling(player)) {
            EssenceService.endDistilling(player);
        }
    }

    private static void checkLethalState(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (BodyService.get(player).isExhausted()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.LIFESPAN_EXHAUSTED), Float.MAX_VALUE);
            return;
        }
        if (SoulService.get(player).isCollapsed()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.SOUL_COLLAPSE), Float.MAX_VALUE);
            return;
        }
        if (MindService.get(player).isOverflowing()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.MIND_OCEAN_SHATTERED), Float.MAX_VALUE);
        }
    }
}

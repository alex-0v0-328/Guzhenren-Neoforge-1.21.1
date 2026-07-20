package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageTick;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

//  The heartbeat: aging, essence and thought regen, lethal checks.
//  Sleep recovery is an edge, not a level -- see PlayerDataEvents.onWakeUp.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        //  Per-second, not per-tick: aging only needs a day boundary, regen carries its own remainder.
        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        //  A Gu goes hungry on the same day rollover that ages its owner -- one clock, two readers:
        //  what he carries starves, what an aperture holds gets fed from his pack first.
        long days = BodyService.tickAging(player);
        if (days > 0L) {
            RefinableGuItem.starveAll(player, days);
            ApertureStorageTick.tickDay(player, days);
        }

        EssenceService.regenStep(player);
        MindService.regenStep(player);
        checkLethalState(player);
    }

    //  Lifespan gone, soul collapsed, or Mind Ocean burst. onRespawn stops the death loop.
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

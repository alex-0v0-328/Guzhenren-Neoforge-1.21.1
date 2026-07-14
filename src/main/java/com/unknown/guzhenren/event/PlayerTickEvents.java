package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.MindService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

//  The heartbeat: aging, essence regen, and the three ways a cultivator runs out of something vital.
//
//  Sleep recovery is NOT here -- it is an edge, not a level. See PlayerDataEvents.onWakeUp.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        //  Per-second, not per-tick. Aging only needs to notice a day boundary and regen carries its
        //  own remainder, so the coarser step costs nothing and saves twenty times the packets.
        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        LifespanService.tickAging(player);
        EssenceService.regenStep(player);
        checkLethalState(player);
    }

    //  Lifespan gone, soul collapsed, or the Mind Ocean burst. PlayerDataService.onRespawn is what
    //  stops any of them becoming a death loop.
    private static void checkLethalState(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (LifespanService.get(player).isExhausted()) {
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

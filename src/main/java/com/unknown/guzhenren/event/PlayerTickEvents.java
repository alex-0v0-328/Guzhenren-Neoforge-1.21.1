package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

//  The heartbeat of the player-data systems: aging, essence regen, and the two ways a cultivator can
//  run out of something vital.
//
//  Sleep recovery is NOT here -- it is an edge, not a level, and lives in PlayerDataEvents.onWakeUp.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        //  Everything here is per-second, not per-tick. Aging only needs to notice a day boundary,
        //  and essence regen carries its own sub-integer remainder, so a coarser step costs nothing
        //  and saves twenty times the packets.
        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        LifespanService.tickAging(player);
        EssenceService.regenStep(player);
        checkLethalState(player);
    }

    //  Lifespan exhausted, or the soul collapsed. Both are lethal.
    //  PlayerDataService.onRespawn is what stops this from becoming a death loop.
    private static void checkLethalState(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (LifespanService.get(player).isExhausted()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.LIFESPAN_EXHAUSTED), Float.MAX_VALUE);
            return;
        }

        if (SoulService.get(player).isCollapsed()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.SOUL_COLLAPSE), Float.MAX_VALUE);
        }
    }
}

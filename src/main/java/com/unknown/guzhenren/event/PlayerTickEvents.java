package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.PlayerDataService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.registry.ModDamageTypes;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

//  The heartbeat of the player-data systems: ageing, essence regen, sleep recovery, and the two
//  ways a cultivator can run out of something vital.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    //  Players who have already been paid out for the sleep they are currently in.
    //
    //  Player.sleepCounter clamps at 100 and stays there for the rest of the sleep, so it is a
    //  level and not an edge -- there is no tick where "just finished sleeping" is true exactly
    //  once. This set is what turns it into a once-per-sleep event.
    //  Server thread only; entries are dropped on wake and on logout.
    private static final Set<UUID> SLEEP_GRANTED = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        handleSleep(player);

        //  Everything below is per-second, not per-tick. Ageing only needs to notice a day boundary,
        //  and essence regen carries its own sub-integer remainder, so a coarser step costs nothing
        //  and saves twenty times the packets.
        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        LifespanService.tickAging(player);
        EssenceService.regenStep(player);
        checkLethalState(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SLEEP_GRANTED.remove(event.getEntity().getUUID());
    }

    private static void handleSleep(ServerPlayer player) {
        if (!player.isSleeping()) {
            SLEEP_GRANTED.remove(player.getUUID());
            return;
        }

        //  add() returns false if we already paid out for this sleep.
        if (player.isSleepingLongEnough() && SLEEP_GRANTED.add(player.getUUID())) {
            PlayerDataService.onSleepComplete(player);
        }
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

package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.PlayerDataService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

//  The player-data lifecycle: the moments that are not a tick.
//
//  There is deliberately no login / respawn / dimension-change resync handler here -- NeoForge already
//  re-sends the full set at all three. See CLAUDE.md "Networking".
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerDataEvents {

    private PlayerDataEvents() {}

    //  Death respawns and non-death clones (End portal return) alike. copyOnDeath would cover the
    //  first; copying here explicitly covers the second and does not depend on NeoForge's internals.
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        PlayerDataService.copy(event.getOriginal(), event.getEntity());
    }

    //  Runs after the clone above, so it inspects the lifespan and soul the player actually kept.
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataService.onRespawn(player);
        }
    }

    //  Only a natural wake pays out. Sleep ends in four ways and the two booleans separate them:
    //
    //    the night passed, the level wakes everyone   stopSleepInBed(false, false)   <-- pay out
    //    the player pressed "Leave Bed"               stopSleepInBed(false, true)
    //    the player logged out while asleep           stopSleepInBed(true,  false)
    //    the player was teleported out of the bed     stopSleepInBed(true,  true)
    //
    //  isSleepingLongEnough() is still true here (the event fires before the counter resets) and
    //  closes the last hole: with playersSleepingPercentage below 100 one deep sleeper skips the night
    //  for everybody, and someone who just climbed into bed must not be paid for it.
    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.wakeImmediately() || event.updateLevel()) return;
        if (!player.isSleepingLongEnough()) return;

        PlayerDataService.onSleepComplete(player);
    }
}

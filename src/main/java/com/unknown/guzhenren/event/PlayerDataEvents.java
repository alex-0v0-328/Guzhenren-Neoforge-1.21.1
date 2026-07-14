package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.PlayerDataService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

//  The player-data lifecycle: the moments that are not a tick.
//
//  No login / dimension-change *resync* handler on purpose -- NeoForge re-sends the full set at both.
//  See CLAUDE.md "Networking".
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerDataEvents {

    private PlayerDataEvents() {}

    //  Death respawns and non-death clones (End portal return) alike. keepInventory is read off the
    //  server, not level(), to sidestep the Level-AutoCloseable gotcha (see CLAUDE.md).
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        MinecraftServer server = event.getOriginal().getServer();
        boolean keepInventory = server != null
                && server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        PlayerDataService.onClone(event.getOriginal(), event.getEntity(), event.isWasDeath(), keepInventory);
    }

    //  Runs after the clone, so it inspects what the player actually kept.
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataService.onRespawn(player);
        }
    }

    //  Only a natural wake pays out. Sleep ends four ways; the two booleans separate them:
    //
    //    night passed, the level wakes everyone   stopSleepInBed(false, false)   <-- pay out
    //    pressed "Leave Bed"                      stopSleepInBed(false, true)
    //    logged out while asleep                  stopSleepInBed(true,  false)
    //    teleported out of the bed                stopSleepInBed(true,  true)
    //
    //  isSleepingLongEnough() is still true here (the event fires before the counter resets) and closes
    //  the last hole: below playersSleepingPercentage 100, one sleeper skips the night for everybody.
    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.wakeImmediately() || event.updateLevel()) return;
        if (!player.isSleepingLongEnough()) return;

        PlayerDataService.onSleepComplete(player);
    }
}

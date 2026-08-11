package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.PlayerDataService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/**
 * The player lifecycle moments, each one forwarded to the single cross-domain service.
 *
 * <p>⚠ Nothing is decided in this file. A handler that starts deciding for itself is how two of them
 * come to disagree about what a respawn keeps.
 *
 * @author Alex
 * @since 1.0.0
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerDataEvents {

    private PlayerDataEvents() {}

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataService.onJoin(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataService.onDeath(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        MinecraftServer server = event.getOriginal().getServer();
        boolean keepInventory = server != null
                && server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        PlayerDataService.onClone(event.getOriginal(), event.getEntity(), event.isWasDeath(), keepInventory);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataService.onRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.wakeImmediately() || event.updateLevel()) return;
        if (!player.isSleepingLongEnough()) return;

        PlayerDataService.onSleepComplete(player);
    }
}

package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.PlayerDataService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

//  What is *not* here is the point: no login, respawn or dimension-change resync handler.
//  The attachments declare .sync(), and vanilla is patched to call syncInitialPlayerAttachments at
//  exactly those three moments (PlayerList#placeNewPlayer, PlayerList#respawn,
//  ServerPlayer#changeDimension). Adding our own would only send the same data twice.
//
//  What remains is the two things NeoForge cannot know about: carrying data across a clone, and
//  making sure a player who died of a data condition is not still in that condition on respawn.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerDataEvents {

    private PlayerDataEvents() {}

    //  Fires for death respawns and non-death clones (End portal return) alike. The attachments are
    //  declared copyOnDeath too, but copying here explicitly covers the non-death case as well and
    //  keeps the behavior independent of NeoForge's clone internals.
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
}

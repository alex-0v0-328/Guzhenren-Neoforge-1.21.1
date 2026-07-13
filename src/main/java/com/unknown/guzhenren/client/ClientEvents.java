package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.hud.PlayerStatsHud;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//  Everything the client has to register: GUI layers today, key mappings and renderers later.
//
//  NeoForge routes each event to the right bus by itself (naming the bus explicitly is deprecated for
//  removal), so this one class can hold startup events and game events side by side. If it ever grows
//  a game-bus half worth separating, split it then -- not before.
@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    private static final ResourceLocation PLAYER_STATS =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "player_stats");

    //  Above the hotbar rather than above everything, so chat, the debug overlay and any open screen
    //  still draw on top of us. The HUD is standing information; it must never cover something the
    //  player is actually reading.
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, PLAYER_STATS, PlayerStatsHud.INSTANCE);
    }
}

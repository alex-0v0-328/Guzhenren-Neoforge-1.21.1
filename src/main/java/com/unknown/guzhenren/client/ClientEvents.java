package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.hud.PlayerStatsHud;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//  Everything the client registers: GUI layers today, keybinds and renderers later.
//
//  NeoForge routes each event to the right bus itself, so startup and game events can sit side by
//  side here. Split when there is a reason, not before.
@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    private static final ResourceLocation PLAYER_STATS =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "player_stats");

    //  Above the hotbar, not above everything: chat, F3 and any open screen still draw on top.
    //  Standing information must never cover what the player is actually reading.
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, PLAYER_STATS, PlayerStatsHud.INSTANCE);
    }
}

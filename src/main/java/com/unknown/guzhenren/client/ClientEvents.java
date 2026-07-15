package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.hud.PlayerStatsHud;
import com.unknown.guzhenren.client.screen.PlayerInfoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//  Everything the client registers: GUI layers and keybinds today, renderers later.
//  NeoForge routes each event to the right bus, so all can sit here. Split when there's a reason.
@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    private static final ResourceLocation PLAYER_STATS =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "player_stats");

    //  Above the hotbar, not above everything: chat, F3 and open screens still draw on top.
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, PLAYER_STATS, PlayerStatsHud.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_INFO);
    }

    //  Open on the keybind, only when no other screen owns the input; the panel closes on the same key.
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        while (ModKeyMappings.OPEN_INFO.consumeClick()) {
            if (minecraft.screen == null) minecraft.setScreen(new PlayerInfoScreen());
        }
    }
}

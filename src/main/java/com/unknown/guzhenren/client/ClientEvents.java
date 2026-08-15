package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.hud.ChargeHud;
import com.unknown.guzhenren.client.hud.NourishHud;
import com.unknown.guzhenren.client.hud.PlayerStatsHud;
import com.unknown.guzhenren.client.screen.ApertureStorageScreen;
import com.unknown.guzhenren.client.screen.PlayerInfoScreen;
import com.unknown.guzhenren.client.screen.RefinementScreen;
import com.unknown.guzhenren.registry.ModEntityTypes;
import com.unknown.guzhenren.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Every client-side registration this mod makes: HUD layers, key mappings, screens and renderers.
 *
 * @author Alex
 * @since 1.0.0
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    private static final ResourceLocation PLAYER_STATS =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "player_stats");

    private static final ResourceLocation CHARGE =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "charge");

    private static final ResourceLocation NOURISH =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "nourish");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, PLAYER_STATS, PlayerStatsHud.INSTANCE);
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, CHARGE, ChargeHud.INSTANCE);
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, NOURISH, NourishHud.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_INFO);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.APERTURE_STORAGE_MENU.get(), ApertureStorageScreen::new);
        event.register(ModMenus.REFINEMENT_MENU.get(), RefinementScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.HOPE_GU_ENTITY.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        while (ModKeyMappings.OPEN_INFO.consumeClick()) {
            if (minecraft.screen == null) minecraft.setScreen(new PlayerInfoScreen());
        }
    }
}

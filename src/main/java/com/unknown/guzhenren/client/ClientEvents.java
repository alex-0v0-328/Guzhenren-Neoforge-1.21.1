package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.client.hud.ChargeHud;
import com.unknown.guzhenren.client.hud.NourishHud;
import com.unknown.guzhenren.client.hud.PlayerStatsHud;
import com.unknown.guzhenren.client.screen.ApertureStorageScreen;
import com.unknown.guzhenren.client.screen.PlayerInfoScreen;
import com.unknown.guzhenren.client.screen.RefinementScreen;
import com.unknown.guzhenren.item.gu.MortalGuItem;
import com.unknown.guzhenren.network.payload.CrashStepPayload;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModEntityTypes;
import com.unknown.guzhenren.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/**
 * Every client-side registration this mod makes: HUD layers, key mappings, screens and renderers.
 *
 * <p>Annotated {@code @EventBusSubscriber(Dist.CLIENT)}. Registers three GUI layers
 * ({@link com.unknown.guzhenren.client.hud.PlayerStatsHud},
 * {@link com.unknown.guzhenren.client.hud.ChargeHud},
 * {@link com.unknown.guzhenren.client.hud.NourishHud}), the key mapping for the B panel, the menu
 * screens for the two containers, and the {@code NoopRenderer} for wild Gu entities.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.client.ModKeyMappings
 * @since 1.0.0
 */

@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    private static final ResourceLocation PLAYER_STATS =
            Guzhenren.id("player_stats");

    private static final ResourceLocation CHARGE =
            Guzhenren.id("charge");

    private static final ResourceLocation NOURISH =
            Guzhenren.id("nourish");

    private static boolean previousUp;
    private static boolean previousDown;
    private static boolean previousLeft;
    private static boolean previousRight;

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
        ItemStack mainHand = minecraft.player.getMainHandItem();
        boolean canUseCrashStep = canUseCrashStep(mainHand);
        if (!canUseCrashStep) {
            EpicFightCapabilities.getLocalPlayerPatchAsOptional(minecraft.player)
                    .filter(yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch::isEpicFightMode)
                    .ifPresent(patch -> patch.toVanillaMode(true));
        }
        while (ModKeyMappings.OPEN_INFO.consumeClick()) {
            if (minecraft.screen == null) minecraft.setScreen(new PlayerInfoScreen());
        }

        boolean up = minecraft.options.keyUp.isDown();
        boolean down = minecraft.options.keyDown.isDown();
        boolean left = minecraft.options.keyLeft.isDown();
        boolean right = minecraft.options.keyRight.isDown();
        boolean pressed = up && !previousUp || down && !previousDown
                || left && !previousLeft || right && !previousRight;
        if (canUseCrashStep && minecraft.screen == null && Screen.hasAltDown() && pressed) {
            int vertical = up == down ? 0 : up ? 1 : -1;
            int horizontal = left == right ? 0 : left ? 1 : -1;
            boolean directionHasEffect = canUseCrashStep(mainHand, vertical, horizontal,
                    minecraft.player.hasEffect(ModEffects.HORIZONTAL_CRASH_GU),
                    minecraft.player.hasEffect(ModEffects.VERTICAL_CRASH_GU),
                    minecraft.player.hasEffect(ModEffects.CHARGING_CRASH_GU));
            EpicFightCapabilities.getLocalPlayerPatchAsOptional(minecraft.player)
                    .filter(patch -> shouldSendCrashStep(patch.isVanillaMode(), directionHasEffect))
                    .ifPresent(patch -> {
                        patch.toEpicFightMode(true);
                        float cameraYRot = EpicFightCameraAPI.getInstance().getForwardYRot();
                        float yRot = Mth.wrapDegrees(cameraYRot
                                - (90.0F * horizontal * (1 - Math.abs(vertical))
                                + 45.0F * vertical * horizontal));
                        PacketDistributor.sendToServer(new CrashStepPayload(vertical, horizontal, yRot));
                    });
        }
        previousUp = up;
        previousDown = down;
        previousLeft = left;
        previousRight = right;
    }

    public static boolean canUseCrashStep(ItemStack mainHand) {
        return !(mainHand.getItem() instanceof MortalGuItem);
    }

    public static boolean canUseCrashStep(ItemStack mainHand, int vertical, int horizontal,
                                          boolean horizontalCrash, boolean verticalCrash, boolean chargingCrash) {
        if (!canUseCrashStep(mainHand) || vertical == 0 && horizontal == 0) return false;
        return (horizontal == 0 || horizontalCrash || chargingCrash)
                && (vertical == 0 || verticalCrash || chargingCrash);
    }

    public static boolean shouldSendCrashStep(boolean vanillaMode, boolean directionHasEffect) {
        return vanillaMode && directionHasEffect;
    }
}

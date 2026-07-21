package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

//  ⚠ The mod's ONE network registration, and the documented exception to "there is no networking".
//  The ban exists so nobody hand-rolls what attachment sync gives free; a CLIENT INTENT is not that --
//  attachment sync is server->client only and cannot carry a choice upstream. Two exist: open my
//  storage, and set my secondary path.
//  ⚠ Downstream player data may never be added here.  CLAUDE.md "Networking".
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class ModPayloads {

    private ModPayloads() {}

    private static final String VERSION = "1";
    private static final String MENU_TITLE = "guzhenren.menu.aperture_storage";

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToServer(OpenApertureStoragePayload.TYPE, OpenApertureStoragePayload.STREAM_CODEC,
                ModPayloads::openStorage);
        registrar.playToServer(SetSecondaryPathPayload.TYPE, SetSecondaryPathPayload.STREAM_CODEC,
                ModPayloads::setSecondaryPath);
    }

    //  ⚠ Never trust the wire: the secondary path is the player's own choice, but the index still has
    //  to exist, and Aperture's ctor is what refuses one equal to the primary -- not this handler.
    private static void setSecondaryPath(SetSecondaryPathPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        int aperture = payload.aperture();
        if (aperture < 0 || aperture >= ApertureService.get(player).count()) return;

        ApertureService.setSecondaryPath(player, aperture, payload.path());
    }

    //  ⚠ Never trust the index off the wire: an unopened aperture has no storage to show.
    private static void openStorage(OpenApertureStoragePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        int aperture = payload.aperture();
        if (aperture < 0 || aperture >= ApertureService.get(player).count()) return;

        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new ApertureStorageMenu(id, inventory, aperture, 0),
                Component.translatable(MENU_TITLE)));
    }
}

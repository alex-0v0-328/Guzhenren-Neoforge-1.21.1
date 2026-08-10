package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ApertureStorageTick {

    private ApertureStorageTick() {}

    public static void tickStored(ServerPlayer player, long days) {
        for (int aperture = 0; aperture < ApertureData.MAX_APERTURES; aperture++) {
            tickStore(player, aperture, days);
            tickVital(player, aperture, days);
        }
    }

    private static void tickStore(ServerPlayer player, int aperture, long days) {
        List<ItemStack> items = ApertureStorageService.items(player, aperture);
        if (items.isEmpty()) return;

        List<ItemStack> next = new ArrayList<>(items);
        boolean changed = false;

        for (int i = 0; i < next.size(); i++) {
            ItemStack stack = next.get(i);
            if (!(stack.getItem() instanceof TendedGuItem gu) || !gu.refined(stack)) continue;

            changed = true;
            if (TendedGuItem.tickInContainer(player, stack, days)) {
                next.set(i, ItemStack.EMPTY);
                TendedGuItem.starved(player, stack);
            }
        }
        if (changed) ApertureStorageService.set(player, aperture, next);
    }

    private static void tickVital(ServerPlayer player, int aperture, long days) {
        ItemStack stack = ApertureStorageService.vital(player, aperture);
        if (!(stack.getItem() instanceof TendedGuItem)) return;

        if (TendedGuItem.tickInContainer(player, stack, days)) {
            ApertureStorageService.setVital(player, aperture, ItemStack.EMPTY);
            TendedGuItem.starved(player, stack);
            return;
        }
        ApertureStorageService.setVital(player, aperture, stack);
    }
}

package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The day-rollover walk over Gu held inside apertures, both the stored ones and each Vital Gu [本命蛊].
 *
 * <p>Static service called from the heartbeat ({@code PlayerTickEvents}) once a day with the elapsed
 * day count. It forwards to {@link TendedGuItem#tickInContainer} for each refined Gu, and reports a
 * starved Gu back through {@link TendedGuItem#starved} -- it never clears a slot itself, the container
 * does.
 *
 * <p>⚠ Every reader here asks {@code refined()} first, because an unrefined Gu's hunger is zero and
 * zero is also what starvation looks like. Drop that test and the first rollover eats every wild Gu.
 * ⚠ The Vital slot write-back ({@code setVital}) runs EVEN WHEN nothing changed -- a single slot has
 * no list, so the store loop's "write whole list" path does not cover it. ⚠ This is one of the three
 * services that imports {@code item/**} on purpose; do not "fix" that import.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see ApertureStorageService
 * @see TendedGuItem
 */
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

            ItemStack before = stack.copy();
            boolean starved = TendedGuItem.tickInContainer(player, stack, days);
            if (starved) {
                next.set(i, ItemStack.EMPTY);
                TendedGuItem.starved(player, stack);
            }
            changed |= starved || changed(before, stack);
        }
        if (changed) ApertureStorageService.set(player, aperture, next);
    }

    private static void tickVital(ServerPlayer player, int aperture, long days) {
        ItemStack stack = ApertureStorageService.vital(player, aperture);
        if (!(stack.getItem() instanceof TendedGuItem)) return;

        ItemStack before = stack.copy();
        if (TendedGuItem.tickInContainer(player, stack, days)) {
            ApertureStorageService.setVital(player, aperture, ItemStack.EMPTY);
            TendedGuItem.starved(player, stack);
            return;
        }
        if (changed(before, stack)) ApertureStorageService.setVital(player, aperture, stack);
    }

    private static boolean changed(ItemStack before, ItemStack after) {
        return before.getCount() != after.getCount()
                || !ItemStack.isSameItemSameComponents(before, after);
    }
}

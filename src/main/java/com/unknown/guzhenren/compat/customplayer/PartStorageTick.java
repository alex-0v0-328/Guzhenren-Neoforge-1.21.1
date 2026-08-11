package com.unknown.guzhenren.compat.customplayer;

import com.unknown.customplayer.attachment.data.body.PartStorage;
import com.unknown.customplayer.attachment.service.body.PartStorageService;
import com.unknown.customplayer.custom.enums.body.BodyPart;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The day-rollover walk over Gu installed into the body parts that the other mod owns.
 *
 * <p>⚠ An installed Gu is kept, so it still eats. A Gu that remodels a limb lives inside that limb
 * rather than being carried, and it would quietly starve without this walk.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class PartStorageTick {

    private PartStorageTick() {}

    public static void tickInstalled(ServerPlayer player, long days) {
        PartStorage storage = PartStorageService.get(player);
        if (storage.isEmpty()) return;

        PartStorage next = storage;
        boolean changed = false;

        for (Map.Entry<BodyPart, ItemStack> installed : storage.installed().entrySet()) {
            ItemStack stack = installed.getValue().copy();
            if (!(stack.getItem() instanceof TendedGuItem)) continue;

            changed = true;
            if (TendedGuItem.tickInContainer(player, stack, days)) {
                next = next.with(installed.getKey(), ItemStack.EMPTY);
                TendedGuItem.starved(player, stack);
            } else {
                next = next.with(installed.getKey(), stack);
            }
        }
        if (changed) PartStorageService.store(player, next);
    }
}

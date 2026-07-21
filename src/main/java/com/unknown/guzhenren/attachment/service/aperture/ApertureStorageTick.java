package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.item.RefinableGuItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

//  The day rollover for Gu kept inside an aperture: bill the days, then feed whatever went hungry.
//  ⚠ Lives beside the storage service, not on the item, because it walks the STORE -- the inventory
//  walk stays on RefinableGuItem. Both are called from PlayerTickEvents on the same clock.
public final class ApertureStorageTick {

    private ApertureStorageTick() {}

    //  ⚠ Decay first, feed second. "Food means it never goes hungry" is the net effect of those two,
    //  not a skipped decay -- so a stored Gu obeys exactly the same rule as one in the hand.
    public static void tickDay(ServerPlayer player, long days) {
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
            if (!(stack.getItem() instanceof RefinableGuItem gu) || !gu.refined(stack)) continue;

            if (gu.decay(stack, days)) {
                next.set(i, ItemStack.EMPTY);
                changed = true;
                RefinableGuItem.starved(player, stack);
                continue;
            }
            changed = true;
            if (gu.autoFeed(player, stack)) continue;

            //  No food anywhere: it keeps sliding, and the warning is the only notice he gets.
            if (gu.hungry(stack)) RefinableGuItem.announceHungry(player, stack);
        }
        if (changed) ApertureStorageService.set(player, aperture, next);
    }

    //  The Vital Gu obeys the identical rule -- binding buys no immunity, and starving it is what costs.
    private static void tickVital(ServerPlayer player, int aperture, long days) {
        ItemStack stack = ApertureStorageService.vital(player, aperture);
        if (!(stack.getItem() instanceof RefinableGuItem gu) || !gu.refined(stack)) return;

        if (gu.decay(stack, days)) {
            ApertureStorageService.setVital(player, aperture, ItemStack.EMPTY);
            RefinableGuItem.starved(player, stack);
            return;
        }
        if (!gu.autoFeed(player, stack) && gu.hungry(stack)) RefinableGuItem.announceHungry(player, stack);

        //  ⚠ Written last, after every mutation -- the store loop can rely on the list aliasing its
        //  stacks, a single slot must not.
        ApertureStorageService.setVital(player, aperture, stack);
    }
}

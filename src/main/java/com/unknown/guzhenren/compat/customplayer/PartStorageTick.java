package com.unknown.guzhenren.compat.customplayer;

import com.unknown.customplayer.attachment.data.body.PartStorage;
import com.unknown.customplayer.attachment.service.body.PartStorageService;
import com.unknown.customplayer.custom.enums.body.BodyPart;
import com.unknown.guzhenren.item.RefinableGuItem;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

//  The day rollover for Gu installed INTO the body [手臂/腿部 等部位]. CustomPlayer owns the storage; the
//  feeding rule is ours, and it is the aperture store's rule verbatim -- RefinableGuItem.tickKept.
//  ⚠ compat/ because the package exists only to reach another mod's surface. CustomPlayer is `required`,
//  so unlike JEI this needs no ModList guard. ⚠ Curios slots hold EQUIPMENT, not Gu -- they get nothing.
public final class PartStorageTick {

    private PartStorageTick() {}

    public static void tickDay(ServerPlayer player, long days) {
        PartStorage storage = PartStorageService.get(player);
        if (storage.isEmpty()) return;

        PartStorage next = storage;
        boolean changed = false;

        for (Map.Entry<BodyPart, ItemStack> installed : storage.installed().entrySet()) {
            //  ⚠⚠ A COPY, deliberately. PartStorage hands out copies and copies again in its ctor, so a
            //  hunger written into the stored instance would reach the player only by aliasing luck.
            //  Mutate the copy, then put it back through with() -- explicit beats relying on that.
            ItemStack stack = installed.getValue().copy();
            if (!(stack.getItem() instanceof RefinableGuItem)) continue;

            changed = true;
            if (RefinableGuItem.tickKept(player, stack, days)) {
                next = next.with(installed.getKey(), ItemStack.EMPTY);
                RefinableGuItem.starved(player, stack);
            } else {
                next = next.with(installed.getKey(), stack);
            }
        }
        if (changed) PartStorageService.store(player, next);
    }
}

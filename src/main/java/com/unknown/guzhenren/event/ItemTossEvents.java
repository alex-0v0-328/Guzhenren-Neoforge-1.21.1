package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.GuItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

//  A Vital Gu cannot be thrown away. MortalGuItem.onDroppedByPlayer covers only the Q-drop of the
//  selected hotbar slot; every path through a container GUI arrives here instead.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class ItemTossEvents {

    private ItemTossEvents() {}

    //  ⚠ Canceling alone would DELETE it: CommonHooks.onPlayerTossEvent removes the stack from the
    //  player first and posts this afterwards, so the handler owns the only copy and must hand it back.
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!GuItem.isVital(stack)) return;

        event.setCanceled(true);
        event.getPlayer().getInventory().placeItemBackInInventory(stack);
    }
}

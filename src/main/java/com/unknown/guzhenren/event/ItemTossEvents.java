package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.GuItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/**
 * The GUI half of "cannot be thrown away"; the hotbar drop is answered on the item itself.
 *
 * <p>⚠ This handler has to hand the stack back. Vanilla removes it before posting the event, so
 * cancelling on its own deletes the item instead of saving it.
 *
 * @author Alex
 * @since 1.0.0
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class ItemTossEvents {

    private ItemTossEvents() {}

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!GuItem.isVital(stack)) return;

        event.setCanceled(true);
        event.getPlayer().getInventory().placeItemBackInInventory(stack);
    }
}

package com.unknown.guzhenren.item.gu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.client.event.ClientEvents;
import com.unknown.guzhenren.registry.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class DashInputTest {

    @Test
    void onlyMainHandGuDisablesDash() {
        assertFalse(ClientEvents.canDash(new ItemStack(ModItems.HORIZONTAL_CRASH_GU.get())));
        assertTrue(ClientEvents.canDash(ItemStack.EMPTY));
        assertTrue(ClientEvents.canDash(new ItemStack(Items.STICK)));
    }

    @Test
    void directionNeedsMatchingCrashEffect() {
        assertTrue(ClientEvents.canDash(ItemStack.EMPTY, 0, 1, true, false, false));
        assertTrue(ClientEvents.canDash(ItemStack.EMPTY, 1, 0, false, true, false));
        assertFalse(ClientEvents.canDash(ItemStack.EMPTY, 1, 0, true, false, false));
        assertFalse(ClientEvents.canDash(ItemStack.EMPTY, 1, 1, true, false, false));
        assertTrue(ClientEvents.canDash(ItemStack.EMPTY, 1, 1, false, false, true));
        assertFalse(ClientEvents.canDash(new ItemStack(ModItems.HORIZONTAL_CRASH_GU.get()),
                0, 1, true, false, false));
    }

    @Test
    void directionWithEffectSendsDash() {
        assertTrue(ClientEvents.shouldSendDash(true));
        assertFalse(ClientEvents.shouldSendDash(false));
    }

    @Test
    void altPressStartsDashWhileDirectionIsHeld() {
        assertTrue(ClientEvents.shouldStartDash(true, false, false));
    }

    @Test
    void directionPressStartsDashWhileAltIsHeld() {
        assertTrue(ClientEvents.shouldStartDash(true, true, true));
    }

    @Test
    void heldKeysDoNotRepeatDash() {
        assertFalse(ClientEvents.shouldStartDash(true, true, false));
        assertFalse(ClientEvents.shouldStartDash(false, false, true));
    }
}

package com.unknown.guzhenren.item.gu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.client.ClientEvents;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class CrashStepInputTest {

    @Test
    void onlyMainHandGuDisablesCrashStep() {
        assertFalse(ClientEvents.canUseCrashStep(new ItemStack(ModItems.HORIZONTAL_CRASH_GU.get())));
        assertTrue(ClientEvents.canUseCrashStep(ItemStack.EMPTY));
        assertTrue(ClientEvents.canUseCrashStep(new ItemStack(Items.STICK)));
    }

    @Test
    void directionNeedsMatchingCrashEffect() {
        assertTrue(ClientEvents.canUseCrashStep(ItemStack.EMPTY, 0, 1, true, false, false));
        assertTrue(ClientEvents.canUseCrashStep(ItemStack.EMPTY, 1, 0, false, true, false));
        assertFalse(ClientEvents.canUseCrashStep(ItemStack.EMPTY, 1, 0, true, false, false));
        assertFalse(ClientEvents.canUseCrashStep(ItemStack.EMPTY, 1, 1, true, false, false));
        assertTrue(ClientEvents.canUseCrashStep(ItemStack.EMPTY, 1, 1, false, false, true));
        assertFalse(ClientEvents.canUseCrashStep(new ItemStack(ModItems.HORIZONTAL_CRASH_GU.get()),
                0, 1, true, false, false));
    }

    @Test
    void battleModeKeepsEpicFightOwnership() {
        assertTrue(ClientEvents.shouldSendCrashStep(true, true));
        assertFalse(ClientEvents.shouldSendCrashStep(false, true));
        assertFalse(ClientEvents.shouldSendCrashStep(true, false));
    }
}

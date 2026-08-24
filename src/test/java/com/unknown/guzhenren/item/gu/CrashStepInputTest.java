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
}

package com.unknown.guzhenren.item.gu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.Ticks;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TendedGuItemTest {

    @Test
    void postRefineCooldownIsOneSecond() {
        assertEquals(Ticks.SECOND, TendedGuItem.POST_REFINE_COOLDOWN_TICKS);
    }
    @Test
    void freshlyRefinedGuIsBlockedForTheWholeSettleWindow() {
        ItemCooldowns cooldowns = new ItemCooldowns();

        TendedGuItem.applyPostRefineCooldown(cooldowns, Items.AIR, TendedGuItem.POST_REFINE_COOLDOWN_TICKS);

        assertTrue(cooldowns.isOnCooldown(Items.AIR));
        for (int tick = 1; tick < Ticks.SECOND; tick++) {
            cooldowns.tick();
            assertTrue(cooldowns.isOnCooldown(Items.AIR), "usable again at tick " + tick);
        }
        cooldowns.tick();
        assertFalse(cooldowns.isOnCooldown(Items.AIR));
    }
    @Test
    void refineStampBlocksFirstUseUntilTheWindowElapses() {
        long refinedAt = 1_000L;
        int window = TendedGuItem.POST_REFINE_COOLDOWN_TICKS;

        assertEquals(window, TendedGuItem.stampCooldownLeft(refinedAt, refinedAt, window));
        assertEquals(1, TendedGuItem.stampCooldownLeft(refinedAt + window - 1, refinedAt, window));
        assertEquals(0, TendedGuItem.stampCooldownLeft(refinedAt + window, refinedAt, window));
        assertEquals(0, TendedGuItem.stampCooldownLeft(refinedAt, null, window));
    }
    @Test
    void settlingNeverShortensAnExistingLongerCooldown() {
        ItemCooldowns cooldowns = new ItemCooldowns();
        cooldowns.addCooldown(Items.AIR, 120 * Ticks.SECOND);

        TendedGuItem.applyPostRefineCooldown(cooldowns, Items.AIR, TendedGuItem.POST_REFINE_COOLDOWN_TICKS);

        for (int tick = 0; tick < Ticks.SECOND; tick++) {
            cooldowns.tick();
            assertTrue(cooldowns.isOnCooldown(Items.AIR), "long cooldown was shortened at tick " + tick);
        }
    }
}

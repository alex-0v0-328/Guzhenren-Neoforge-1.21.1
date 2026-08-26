package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.item.GuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeFlowServiceTest {

    /** Every rate a player can reach from the two Watch Gu effects. */
    private static final int[] RATES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13};

    @Test
    @DisplayName("an ordinary clock changes nothing at all")
    void ordinaryClockIsIdentity() {
        assertEquals(20, TimeFlowService.waited(TimeFlowService.NORMAL_RATE, 20));
        assertEquals(7L, TimeFlowService.perStep(TimeFlowService.NORMAL_RATE, 7L));
    }

    @Test
    @DisplayName("the use ladder shortens but never reaches zero")
    void theUseLadderShortens() {
        assertEquals(2, TimeFlowService.waited(2, GuItem.USE_FAST_TICKS));
        assertEquals(5, TimeFlowService.waited(2, GuItem.USE_SAME_TICKS));
        assertEquals(Ticks.HALF_SECOND, TimeFlowService.waited(2, GuItem.USE_SLOW_TICKS));

        assertEquals(1, TimeFlowService.waited(3, GuItem.USE_FAST_TICKS));
        assertEquals(3, TimeFlowService.waited(3, GuItem.USE_SAME_TICKS));
        assertEquals(6, TimeFlowService.waited(3, GuItem.USE_SLOW_TICKS));
    }

    @Test
    @DisplayName("a wait shorter than the rate floors at one tick, never at none")
    void aShortWaitNeverBecomesNoWait() {
        for (int rate : RATES) {
            assertEquals(1, TimeFlowService.waited(rate, 1), "rate " + rate);
            assertTrue(TimeFlowService.waited(rate, GuItem.COOLDOWN_TICKS) >= 1, "rate " + rate);
        }
        assertEquals(0, TimeFlowService.waited(3, 0));
    }

    @Test
    @DisplayName("what a step pays out scales with the rate and nothing else")
    void aStepScalesWithTheRate() {
        for (int rate : RATES) {
            assertEquals(100L * rate, TimeFlowService.perStep(rate, 100L), "rate " + rate);
            assertEquals(0.5D * rate, TimeFlowService.perStep(rate, 0.5D), 0.0D, "rate " + rate);
        }
    }
}

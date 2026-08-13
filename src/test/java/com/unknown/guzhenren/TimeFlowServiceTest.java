package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.item.GuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeFlowServiceTest {

    /** Every rate a player can actually reach: one Watch Gu, the other, or both worn together. */
    private static final int[] RATES = {1, 2, 3, 5};

    @Test
    @DisplayName("an ordinary clock changes nothing at all")
    void ordinaryClockIsIdentity() {
        assertEquals(20, TimeFlowService.waited(TimeFlowService.NORMAL_RATE, 20));
        assertEquals(7L, TimeFlowService.perStep(TimeFlowService.NORMAL_RATE, 7L));
        assertEquals(0L, TimeFlowService.skipped(TimeFlowService.NORMAL_RATE, Ticks.SECOND));
    }

    @Test
    @DisplayName("the use ladder shortens but never reaches zero")
    void theUseLadderShortens() {
        assertEquals(2, TimeFlowService.waited(2, GuItem.USE_FAST_TICKS));
        assertEquals(5, TimeFlowService.waited(2, GuItem.USE_SAME_TICKS));
        assertEquals(10, TimeFlowService.waited(2, GuItem.USE_SLOW_TICKS));

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
    @DisplayName("a heartbeat's skipped share is a whole number of parts at every rate")
    void everyHeartbeatBanksWholeParts() {
        assertEquals(0L, TimeFlowService.skipped(1, Ticks.SECOND));
        assertEquals(60L, TimeFlowService.skipped(2, Ticks.SECOND));
        assertEquals(80L, TimeFlowService.skipped(3, Ticks.SECOND));
        assertEquals(96L, TimeFlowService.skipped(5, Ticks.SECOND));
    }

    @Test
    @DisplayName("banked parts land exactly on a whole day, so no year is ever half billed")
    void bankedPartsLandOnAWholeDay() {
        long partsPerDay = Ticks.DAY * TimeFlowService.PARTS_PER_TICK;
        for (int rate : RATES) {
            long perHeartbeat = TimeFlowService.skipped(rate, Ticks.SECOND);
            if (perHeartbeat == 0L) continue;
            assertEquals(0L, partsPerDay % perHeartbeat, "rate " + rate);
        }
    }

    @Test
    @DisplayName("a day spent hastened forgives exactly the share the rate promised")
    void aHastenedDayForgivesItsShare() {
        long partsPerDay = Ticks.DAY * TimeFlowService.PARTS_PER_TICK;
        long heartbeatsPerDay = Ticks.DAY / Ticks.SECOND;

        assertEquals(partsPerDay / 2, TimeFlowService.skipped(2, Ticks.SECOND) * heartbeatsPerDay);
        assertEquals(partsPerDay * 2 / 3, TimeFlowService.skipped(3, Ticks.SECOND) * heartbeatsPerDay);
        assertEquals(partsPerDay * 4 / 5, TimeFlowService.skipped(5, Ticks.SECOND) * heartbeatsPerDay);
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

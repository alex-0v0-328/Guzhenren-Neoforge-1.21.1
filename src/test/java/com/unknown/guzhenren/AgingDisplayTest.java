package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgingDisplayTest {

    private static final double EXACT = 1.0E-9D;

    private static final long PARTS_PER_DAY = Ticks.DAY * TimeFlowService.PARTS_PER_TICK;

    private static double lived(long dayTime, long lastDayIndex, long hastenedParts) {
        return BodyService.yearsSinceBilled(dayTime, lastDayIndex, hastenedParts);
    }

    @Test
    @DisplayName("a freshly billed year starts at nothing lived and reaches a whole one")
    void aYearRunsFromZeroToOne() {
        assertEquals(0.0D, lived(0L, 0L, 0L), EXACT);
        assertEquals(0.5D, lived(Ticks.HALF_DAY, 0L, 0L), EXACT);
        assertTrue(lived(Ticks.DAY - 1L, 0L, 0L) < 1.0D);
    }

    @Test
    @DisplayName("the second between a rollover and the heartbeat that bills it does not jump a year")
    void therolloverGapDoesNotJump() {
        double last = lived(Ticks.DAY - 1L, 0L, 0L);
        double unbilled = lived(Ticks.DAY + Ticks.SECOND, 0L, 0L);
        assertTrue(unbilled > last, unbilled + " should keep rising past " + last);
        assertTrue(unbilled - last < 0.001D, "jumped by " + (unbilled - last));
    }

    @Test
    @DisplayName("billing a day subtracts exactly one year's worth, so the pair of figures stays put")
    void billingADayIsContinuous() {
        long carry = PARTS_PER_DAY / 4L;
        double before = lived(Ticks.DAY, 0L, carry);
        double after = lived(Ticks.DAY, 1L, carry);
        assertEquals(1.0D, before - after, EXACT);
    }

    @Test
    @DisplayName("banked time slows the figure down, and a whole day of credit stops it dead")
    void bankedTimeSlowsTheFigure() {
        assertEquals(0.1D, lived(Ticks.HALF_DAY, 0L, PARTS_PER_DAY * 2L / 5L), EXACT);
        assertEquals(0.0D, lived(Ticks.HALF_DAY, 0L, PARTS_PER_DAY / 2L), EXACT);
    }

    @Test
    @DisplayName("⚠ carried credit reads NEGATIVE and must never be clamped, or the figure stands still")
    void carriedCreditGoesNegative() {
        assertTrue(lived(0L, 0L, PARTS_PER_DAY * 4L / 5L) < 0.0D);
        assertEquals(-0.8D, lived(0L, 0L, PARTS_PER_DAY * 4L / 5L), EXACT);
    }

    @Test
    @DisplayName("an offline stretch bills every day it covers rather than only the last one")
    void offlineStretchCountsEveryDay() {
        assertEquals(5.0D, lived(5L * Ticks.DAY, 0L, 0L), EXACT);
        assertEquals(5.5D, lived(5L * Ticks.DAY + Ticks.HALF_DAY, 0L, 0L), EXACT);
    }
}

package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.path.PathTimeFlowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgingDisplayTest {

    /** Every rate a player can reach: no Watch Gu, one, the other, or both worn together. */
    private static final int[] RATES = {1, 2, 3, 5};

    private static final long BEAT = Ticks.SECOND;

    /** What one stretch of world time costs him, through the same door every other spend goes through. */
    private static long lived(long elapsedTicks, int rate) {
        return PathTimeFlowService.perStep(rate, BodyService.elapsedParts(elapsedTicks));
    }

    @Test
    @DisplayName("a game day is exactly one year, and a heartbeat is a whole number of parts")
    void aDayIsOneYear() {
        assertEquals(BodyData.PARTS_PER_YEAR, Ticks.DAY * BodyData.PARTS_PER_TICK);
        assertEquals(120L, BodyService.elapsedParts(BEAT));
    }

    @Test
    @DisplayName("☠ a hastened clock spends life FASTER -- the rate multiplies, it never divides")
    void hasteSpendsLifeFaster() {
        long ordinary = lived(BEAT, 1);
        assertEquals(120L, ordinary);

        for (int rate : RATES) {
            assertEquals(ordinary * rate, lived(BEAT, rate), "rate " + rate);
            if (rate > 1) assertTrue(lived(BEAT, rate) > ordinary, "rate " + rate);
        }
    }

    @Test
    @DisplayName("a whole game day costs exactly one year at ordinary speed, and N years at rate N")
    void aDayCostsAYearPerRate() {
        long beatsPerDay = Ticks.DAY / BEAT;
        for (int rate : RATES) {
            assertEquals(BodyData.PARTS_PER_YEAR * rate, lived(BEAT, rate) * beatsPerDay, "rate " + rate);
        }
    }

    @Test
    @DisplayName("one five-minute form costs a quarter of a year per point of rate")
    void oneFormCostsAQuarterYearPerRate() {
        long form = 5L * Ticks.MINUTE;
        assertEquals(BodyData.PARTS_PER_YEAR / 2L, lived(form, 2));
        assertEquals(BodyData.PARTS_PER_YEAR * 3L / 4L, lived(form, 3));
        assertEquals(BodyData.PARTS_PER_YEAR * 5L / 4L, lived(form, 5));
    }

    @Test
    @DisplayName("one long offline stretch bills the same as the beats it stood in for")
    void offlineMatchesBeatByBeat() {
        long beats = 137L;
        for (int rate : RATES) {
            assertEquals(lived(BEAT, rate) * beats, lived(beats * BEAT, rate), "rate " + rate);
        }
    }

    @Test
    @DisplayName("time running backwards bills nothing rather than handing life back")
    void backwardsTimeBillsNothing() {
        for (int rate : RATES) {
            assertEquals(0L, lived(-Ticks.DAY, rate), "rate " + rate);
            assertEquals(0L, lived(0L, rate), "rate " + rate);
        }
    }

    @Test
    @DisplayName("a default body starts on the stated years, not on a bare part count")
    void defaultsAreStatedInYears() {
        assertEquals(BodyData.DEFAULT_AGE, (long) BodyData.DEFAULT.ageYears());
        assertEquals(BodyData.DEFAULT_LIFESPAN, (long) BodyData.DEFAULT.lifespanYears());
        assertTrue(BodyData.DEFAULT.lifespanParts() > 0L);
    }
}

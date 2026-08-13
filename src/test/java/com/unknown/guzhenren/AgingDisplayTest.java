package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgingDisplayTest {

    /** Every rate a player can reach: no Watch Gu, one, the other, or both worn together. */
    private static final int[] RATES = {1, 2, 3, 5};

    private static final long BEAT = Ticks.SECOND;

    @Test
    @DisplayName("a game day is exactly one year, and a heartbeat is a whole number of parts")
    void aDayIsOneYear() {
        assertEquals(BodyData.PARTS_PER_YEAR, Ticks.DAY * BodyData.PARTS_PER_TICK);
        assertEquals(120L, BEAT * BodyData.PARTS_PER_TICK);
    }

    @Test
    @DisplayName("a heartbeat bills a whole number of parts at every rate a player can reach")
    void everyRateDividesAHeartbeat() {
        assertEquals(120L, BodyService.livedParts(BEAT, 1));
        assertEquals(60L, BodyService.livedParts(BEAT, 2));
        assertEquals(40L, BodyService.livedParts(BEAT, 3));
        assertEquals(24L, BodyService.livedParts(BEAT, 5));

        for (int rate : RATES) {
            assertEquals(BEAT * BodyData.PARTS_PER_TICK, BodyService.livedParts(BEAT, rate) * rate,
                    "rate " + rate + " loses a part every beat");
        }
    }

    @Test
    @DisplayName("a whole game day of heartbeats sums to exactly one year, not a part more or less")
    void aDayOfHeartbeatsSumsToOneYear() {
        long beatsPerDay = Ticks.DAY / BEAT;
        for (int rate : RATES) {
            long summed = BodyService.livedParts(BEAT, rate) * beatsPerDay;
            assertEquals(BodyData.PARTS_PER_YEAR / rate, summed, "rate " + rate);
        }
    }

    @Test
    @DisplayName("one long offline stretch bills the same as the beats it stood in for")
    void offlineMatchesBeatByBeat() {
        long beats = 137L;
        for (int rate : RATES) {
            long atOnce = BodyService.livedParts(beats * BEAT, rate);
            long oneByOne = BodyService.livedParts(BEAT, rate) * beats;
            assertEquals(oneByOne, atOnce, "rate " + rate);
        }
    }

    @Test
    @DisplayName("time running backwards bills nothing rather than handing life back")
    void backwardsTimeBillsNothing() {
        for (int rate : RATES) {
            assertEquals(0L, BodyService.livedParts(-Ticks.DAY, rate), "rate " + rate);
            assertEquals(0L, BodyService.livedParts(0L, rate), "rate " + rate);
        }
    }

    @Test
    @DisplayName("a hastened clock spends strictly less life over the same stretch of world time")
    void hasteSpendsLessLife() {
        long ordinary = BodyService.livedParts(Ticks.DAY, 1);
        for (int rate : RATES) {
            if (rate == 1) continue;
            assertTrue(BodyService.livedParts(Ticks.DAY, rate) < ordinary, "rate " + rate);
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

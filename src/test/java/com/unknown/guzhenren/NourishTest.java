package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import com.unknown.guzhenren.attachment.service.aperture.NourishService.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NourishTest {

    @Test
    @DisplayName("冲击窍壁 ordinary table splits at 20 / 50 / 80")
    void ordinaryBoundaries() {
        assertSame(Outcome.SUCCESS, NourishService.resolve(0, false));
        assertSame(Outcome.SUCCESS, NourishService.resolve(19, false));
        assertSame(Outcome.HOLD, NourishService.resolve(20, false));
        assertSame(Outcome.HOLD, NourishService.resolve(49, false));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(50, false));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(79, false));
        assertSame(Outcome.DROP_BASE, NourishService.resolve(80, false));
        assertSame(Outcome.DROP_BASE, NourishService.resolve(99, false));
    }

    @Test
    @DisplayName("十绝体 table splits at 50 / 80 -- the two tables cut at different points")
    void extremeBoundaries() {
        assertSame(Outcome.SUCCESS, NourishService.resolve(0, true));
        assertSame(Outcome.SUCCESS, NourishService.resolve(49, true));
        assertSame(Outcome.HOLD, NourishService.resolve(50, true));
        assertSame(Outcome.HOLD, NourishService.resolve(79, true));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(80, true));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(99, true));
    }

    @Test
    @DisplayName("十绝体 can never roll the aptitude loss")
    void extremeNeverLosesBase() {
        for (int roll = 0; roll < 100; roll++) {
            assertNotSame(Outcome.DROP_BASE, NourishService.resolve(roll, true), "roll " + roll);
        }
    }

    @Test
    @DisplayName("every roll in 0..99 resolves, and the two tables agree on nothing but their shape")
    void everyRollResolves() {
        int ordinarySuccess = 0;
        int extremeSuccess = 0;
        for (int roll = 0; roll < 100; roll++) {
            if (NourishService.resolve(roll, false) == Outcome.SUCCESS) ordinarySuccess++;
            if (NourishService.resolve(roll, true) == Outcome.SUCCESS) extremeSuccess++;
        }
        assertEquals(20, ordinarySuccess);
        assertEquals(50, extremeSuccess);
    }

    @Test
    @DisplayName("progress is clamped, and the starved anchor defaults to the sentinel, never to zero")
    void recordInvariants() {
        assertEquals(0, new NourishData(true, -5, NourishData.NOT_STARVED).progress());
        assertEquals(NourishData.FULL, new NourishData(true, 500, NourishData.NOT_STARVED).progress());
        assertEquals(NourishData.NOT_STARVED, NourishData.DEFAULT.starvedSinceTick());
        assertEquals(false, NourishData.DEFAULT.isStarving());
    }

    @Test
    @DisplayName("a fresh world is not already starved out -- zero is a real game time")
    void freshWorldIsNotStarved() {
        assertEquals(false, NourishData.DEFAULT.starvedOut(0L));
        assertEquals(false, NourishData.DEFAULT.starvedOut(Long.MAX_VALUE / 2));
    }

    @Test
    @DisplayName("the starve grace runs out only after the full window")
    void starveWindow() {
        NourishData starving = NourishData.DEFAULT.withStarvedSince(1_000L);
        assertEquals(false, starving.starvedOut(1_000L + NourishData.STARVE_GRACE_TICKS - 1L));
        assertEquals(true, starving.starvedOut(1_000L + NourishData.STARVE_GRACE_TICKS));
    }
}

package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import com.unknown.guzhenren.attachment.service.aperture.NourishService.Outcome;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NourishTest {

    @Test
    @DisplayName("冲击窍壁 ordinary table splits at 40 / 70 / 90")
    void ordinaryBoundaries() {
        assertSame(Outcome.SUCCESS, NourishService.resolve(0, false));
        assertSame(Outcome.SUCCESS, NourishService.resolve(39, false));
        assertSame(Outcome.HOLD, NourishService.resolve(40, false));
        assertSame(Outcome.HOLD, NourishService.resolve(69, false));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(70, false));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(89, false));
        assertSame(Outcome.DROP_BASE, NourishService.resolve(90, false));
        assertSame(Outcome.DROP_BASE, NourishService.resolve(99, false));
    }

    @Test
    @DisplayName("十绝体 table splits at 60 / 85 -- the two tables cut at different points")
    void extremeBoundaries() {
        assertSame(Outcome.SUCCESS, NourishService.resolve(0, true));
        assertSame(Outcome.SUCCESS, NourishService.resolve(59, true));
        assertSame(Outcome.HOLD, NourishService.resolve(60, true));
        assertSame(Outcome.HOLD, NourishService.resolve(84, true));
        assertSame(Outcome.DROP_STAGE, NourishService.resolve(85, true));
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
        assertEquals(40, ordinarySuccess);
        assertEquals(60, extremeSuccess);
    }

    @Test
    @DisplayName("冲击窍壁 costs one and a half Ten-Extremes peak pools -- the ×10 rank ladder, exactly")
    void impactCostLadder() {
        assertEquals(1_200L, cost(Rank.ONE));
        assertEquals(12_000L, cost(Rank.TWO));
        assertEquals(120_000L, cost(Rank.THREE));
        assertEquals(1_200_000L, cost(Rank.FOUR));
    }

    @Test
    @DisplayName("no pool can hold one strike -- even a 十绝 peak pool is only two thirds of it")
    void noPoolCoversAStrike() {
        for (Rank rank : new Rank[] {Rank.ONE, Rank.TWO, Rank.THREE, Rank.FOUR}) {
            long peak = Aperture.maxEssence(rank, Stage.PEAK, Aperture.MAX_BASE);
            assertEquals(cost(rank) * 2L, peak * 3L, "rank " + rank);
        }
    }

    private static long cost(Rank rank) {
        return NourishService.IMPACT_COST_PER_RANK_BASE * rank.getRankBase();
    }

    @Test
    @DisplayName("progress is clamped, and the starved anchor defaults to the sentinel, never to zero")
    void recordInvariants() {
        assertEquals(0, new NourishData(true, -5, NourishData.NOT_STARVED, false).progress());
        assertEquals(NourishData.FULL, new NourishData(true, 500, NourishData.NOT_STARVED, false).progress());
        assertEquals(NourishData.NOT_STARVED, NourishData.DEFAULT.starvedSinceTick());
        assertEquals(false, NourishData.DEFAULT.isStarving());
    }

    @Test
    @DisplayName("石窍蛊 lock: PETRIFIED stops cultivating and zeroes progress, DEFAULT never carries it")
    void petrifiedInvariants() {
        assertEquals(false, NourishData.DEFAULT.petrified());
        assertEquals(true, NourishData.PETRIFIED.petrified());
        assertEquals(false, NourishData.PETRIFIED.cultivating());
        assertEquals(0, NourishData.PETRIFIED.progress());
        assertEquals(NourishData.NOT_STARVED, NourishData.PETRIFIED.starvedSinceTick());
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

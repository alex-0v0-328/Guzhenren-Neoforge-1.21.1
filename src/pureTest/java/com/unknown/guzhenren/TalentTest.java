package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.unknown.guzhenren.custom.enums.aperture.Talent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TalentTest {

    @Test
    @DisplayName("every band maps back to its own grade at both edges")
    void bandsMapBackAtTheirEdges() {
        assertSame(Talent.FOURTH, Talent.fromPercent(20));
        assertSame(Talent.FOURTH, Talent.fromPercent(39));
        assertSame(Talent.THIRD, Talent.fromPercent(40));
        assertSame(Talent.THIRD, Talent.fromPercent(59));
        assertSame(Talent.SECOND, Talent.fromPercent(60));
        assertSame(Talent.SECOND, Talent.fromPercent(79));
        assertSame(Talent.FIRST, Talent.fromPercent(80));
        assertSame(Talent.FIRST, Talent.fromPercent(99));
        assertSame(Talent.EXTREME, Talent.fromPercent(100));
    }
    @Test
    @DisplayName("1..19 is a HOLE, not a grade -- and 0 belongs to Aperture.NONE alone")
    void theHoleBelowTwenty() {
        assertSame(Talent.NONE, Talent.fromPercent(0));
        for (int percent = 1; percent <= 19; percent++) {
            assertSame(Talent.NONE, Talent.fromPercent(percent), "percent " + percent + " must be a hole");
        }
    }
    @Test
    @DisplayName("out of range never throws, it reads NONE")
    void outOfRangeReadsNone() {
        assertSame(Talent.NONE, Talent.fromPercent(-1));
        assertSame(Talent.NONE, Talent.fromPercent(101));
        assertSame(Talent.NONE, Talent.fromPercent(Integer.MAX_VALUE));
    }
    @Test
    @DisplayName("a POSITIVE shift means BETTER, even though the constants run high to low")
    void positiveShiftMeansBetter() {
        assertSame(Talent.THIRD, Talent.FOURTH.shift(1));
        assertSame(Talent.SECOND, Talent.THIRD.shift(1));
        assertSame(Talent.EXTREME, Talent.FIRST.shift(1));
        assertSame(Talent.FOURTH, Talent.THIRD.shift(-1));
    }
    @Test
    @DisplayName("shift CLAMPS at both ends and never wraps into NONE")
    void shiftClampsAndNeverReachesNone() {
        assertSame(Talent.EXTREME, Talent.EXTREME.shift(1));
        assertSame(Talent.EXTREME, Talent.EXTREME.shift(99));
        assertSame(Talent.FOURTH, Talent.FOURTH.shift(-1));
        assertSame(Talent.FOURTH, Talent.FOURTH.shift(-99));
    }
    @Test
    @DisplayName("the settable range excludes NONE")
    void settableExcludesNone() {
        Talent[] settable = Talent.settable();
        assertEquals(5, settable.length);
        for (Talent grade : settable) {
            assertTrue(grade != Talent.NONE, "NONE must not be settable");
        }
    }
    @Test
    @DisplayName("a rolled percent always reads back as the grade it was rolled for")
    void rolledPercentReadsBack() {
        for (Talent grade : Talent.settable()) {
            for (int i = 0; i < 200; i++) {
                assertSame(grade, Talent.fromPercent(Talent.randomPercent(grade)));
            }
        }
    }
}

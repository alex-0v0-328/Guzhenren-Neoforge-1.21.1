package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.custom.enums.qi.QiKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QiKindTest {

    @Test
    @DisplayName("a 蛊材 of each rank grants 10 / 40 / 160 / 640 / 2560")
    void theTierLadder() {
        assertEquals(10L, QiKind.tierAmount(0));
        assertEquals(40L, QiKind.tierAmount(1));
        assertEquals(160L, QiKind.tierAmount(2));
        assertEquals(640L, QiKind.tierAmount(3));
        assertEquals(2560L, QiKind.tierAmount(4));
    }
    @Test
    @DisplayName("decay is 1 / 2 / 4 / 8 / 16 a second")
    void theDecayLadder() {
        assertEquals(1L, QiKind.decayPerSecond(0));
        assertEquals(2L, QiKind.decayPerSecond(1));
        assertEquals(4L, QiKind.decayPerSecond(2));
        assertEquals(8L, QiKind.decayPerSecond(3));
        assertEquals(16L, QiKind.decayPerSecond(4));
    }
    @Test
    @DisplayName("a full tier drains in 10 / 20 / 40 / 80 / 160 seconds, and the amount divides exactly")
    void everyTierDrainsInWholeSeconds() {
        long[] expected = {10L, 20L, 40L, 80L, 160L};
        for (int tier = 0; tier < expected.length; tier++) {
            long amount = QiKind.tierAmount(tier);
            long perSecond = QiKind.decayPerSecond(tier);
            assertEquals(0L, amount % perSecond, "tier " + tier + " does not divide evenly");
            assertEquals(expected[tier], amount / perSecond, "tier " + tier + " drains in the wrong time");
        }
    }
    @Test
    @DisplayName("tierOf reads the tier off the SUM, and every boundary lands on the right side")
    void tierBoundaries() {
        assertEquals(-1, QiKind.tierOf(0L));
        assertEquals(-1, QiKind.tierOf(9L));
        assertEquals(0, QiKind.tierOf(10L));
        assertEquals(0, QiKind.tierOf(39L));
        assertEquals(1, QiKind.tierOf(40L));
        assertEquals(1, QiKind.tierOf(159L));
        assertEquals(2, QiKind.tierOf(160L));
        assertEquals(3, QiKind.tierOf(640L));
        assertEquals(4, QiKind.tierOf(2560L));
    }
    @Test
    @DisplayName("past the top tier it stays at the top rather than running off the table")
    void tierOfSaturates() {
        assertEquals(4, QiKind.tierOf(1_000_000L));
        assertEquals(4, QiKind.tierOf(Long.MAX_VALUE));
    }
    @Test
    @DisplayName("死/人/天/地 never expire; 剑/力 are flat; 生/元 scale with the tier")
    void whichKindsAreTimed() {
        assertFalse(QiKind.DEATH.isTimed());
        assertFalse(QiKind.HUMAN.isTimed());
        assertFalse(QiKind.HEAVEN.isTimed());
        assertFalse(QiKind.EARTH.isTimed());

        assertTrue(QiKind.SWORD.isTimed());
        assertTrue(QiKind.STRENGTH.isTimed());
        assertTrue(QiKind.LIFE.isTimed());
        assertTrue(QiKind.ESSENCE.isTimed());
    }
    @Test
    @DisplayName("a flat hold ignores the tier; a per-tier hold is (tier + 1) x 2 minutes")
    void holdShapes() {
        assertEquals(5 * Ticks.MINUTE, QiKind.SWORD.holdTicks(0));
        assertEquals(5 * Ticks.MINUTE, QiKind.SWORD.holdTicks(4));
        assertEquals(10 * Ticks.MINUTE, QiKind.STRENGTH.holdTicks(4));

        assertEquals(2 * Ticks.MINUTE, QiKind.LIFE.holdTicks(0));
        assertEquals(10 * Ticks.MINUTE, QiKind.LIFE.holdTicks(4));
        assertEquals(2 * Ticks.MINUTE, QiKind.ESSENCE.holdTicks(0));
    }
}

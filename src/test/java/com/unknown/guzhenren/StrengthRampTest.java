package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StrengthRampTest {

    private static final int MORTAL_CAPACITY = 100;
    private static final int GREAT_STRENGTH_CAPACITY = 300;

    @Test
    @DisplayName("at or under the capacity every 斤 is usable")
    void oneToOneUnderCapacity() {
        assertEquals(0, StrengthService.usableJin(MORTAL_CAPACITY, 0));
        assertEquals(1, StrengthService.usableJin(MORTAL_CAPACITY, 1));
        assertEquals(99, StrengthService.usableJin(MORTAL_CAPACITY, 99));
        assertEquals(100, StrengthService.usableJin(MORTAL_CAPACITY, 100));
    }

    @Test
    @DisplayName("the tail is EARNED -- 101 斤 must not jump to the locked 120")
    void theTailIsEarned() {
        assertEquals(100, StrengthService.usableJin(MORTAL_CAPACITY, 101));
        assertEquals(101, StrengthService.usableJin(MORTAL_CAPACITY, 145));
        assertEquals(110, StrengthService.usableJin(MORTAL_CAPACITY, 550));
    }

    @Test
    @DisplayName("the tail locks at capacity + 20, and stays there forever")
    void theTailLocks() {
        assertEquals(120, StrengthService.usableJin(MORTAL_CAPACITY, 1000));
        assertEquals(120, StrengthService.usableJin(MORTAL_CAPACITY, 9999));
        assertEquals(120, StrengthService.usableJin(MORTAL_CAPACITY, Integer.MAX_VALUE / 2));
    }

    @Test
    @DisplayName("the lock scales with the physique, it is not an absolute 1000")
    void theLockScalesWithCapacity() {
        assertEquals(300, StrengthService.usableJin(GREAT_STRENGTH_CAPACITY, 300));
        assertEquals(300, StrengthService.usableJin(GREAT_STRENGTH_CAPACITY, 301));
        assertEquals(310, StrengthService.usableJin(GREAT_STRENGTH_CAPACITY, 1650));
        assertEquals(320, StrengthService.usableJin(GREAT_STRENGTH_CAPACITY, 3000));
        assertEquals(320, StrengthService.usableJin(GREAT_STRENGTH_CAPACITY, 9999));
    }

    @Test
    @DisplayName("Hardship Strength Gu adds 20 capacity at every crossed health band")
    void hardshipStrengthBands() {
        assertEquals(0, StrengthService.hardshipCapacityBonus(0.6001D));
        assertEquals(20, StrengthService.hardshipCapacityBonus(0.6D));
        assertEquals(40, StrengthService.hardshipCapacityBonus(0.5D));
        assertEquals(60, StrengthService.hardshipCapacityBonus(0.4D));
        assertEquals(80, StrengthService.hardshipCapacityBonus(0.3D));
        assertEquals(100, StrengthService.hardshipCapacityBonus(0.2D));
        assertEquals(120, StrengthService.hardshipCapacityBonus(0.1D));
        assertEquals(120, StrengthService.hardshipCapacityBonus(0.05D));
        assertEquals(120, StrengthService.hardshipCapacityBonus(0.0D));
    }

    @Test
    @DisplayName("Hardship Strength Gu moves the existing ramp instead of bypassing it")
    void hardshipStrengthMovesTheRamp() {
        int capacityAtFivePercent = MORTAL_CAPACITY + StrengthService.hardshipCapacityBonus(0.05D);
        int greatStrengthCapacityAtFivePercent =
                GREAT_STRENGTH_CAPACITY + StrengthService.hardshipCapacityBonus(0.05D);
        assertEquals(220, StrengthService.usableJin(capacityAtFivePercent, 220));
        assertEquals(230, StrengthService.usableJin(capacityAtFivePercent, 1210));
        assertEquals(240, StrengthService.usableJin(capacityAtFivePercent, 2200));
        assertEquals(240, StrengthService.usableJin(capacityAtFivePercent, 9999));
        assertEquals(420, StrengthService.usableJin(greatStrengthCapacityAtFivePercent, 420));
        assertEquals(440, StrengthService.usableJin(greatStrengthCapacityAtFivePercent, 4200));
        assertEquals(440, StrengthService.usableJin(greatStrengthCapacityAtFivePercent, 9999));
    }

    @Test
    @DisplayName("the ramp never goes backwards as 斤 accumulate")
    void theRampIsMonotonic() {
        int previous = 0;
        for (int total = 0; total <= 4000; total++) {
            int usable = StrengthService.usableJin(MORTAL_CAPACITY, total);
            assertTrue(usable >= previous, "usable dropped at total=" + total);
            previous = usable;
        }
    }
}

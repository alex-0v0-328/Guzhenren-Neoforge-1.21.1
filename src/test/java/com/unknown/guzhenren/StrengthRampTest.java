package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @DisplayName("the ramp never goes backwards as 斤 accumulate")
    void theRampIsMonotonic() {
        int previous = 0;
        for (int total = 0; total <= 4000; total++) {
            int usable = StrengthService.usableJin(MORTAL_CAPACITY, total);
            assertEquals(true, usable >= previous, "usable dropped at total=" + total);
            previous = usable;
        }
    }
}

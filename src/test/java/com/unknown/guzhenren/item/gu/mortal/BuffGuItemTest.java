package com.unknown.guzhenren.item.gu.mortal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.item.gu.MortalGuItem;
import org.junit.jupiter.api.Test;

class BuffGuItemTest {

    @Test
    void durationAccumulatesOnlyDuringCooldownAndHigherGradeOverrides() {
        assertEquals(2 * Ticks.SECOND, MortalGuItem.REFINE_DONE_COOLDOWN_TICKS);
        assertEquals(1_200, BuffGuItem.nextDuration(600, 0, 600, 0, true));
        assertEquals(600, BuffGuItem.nextDuration(300, 0, 600, 0, false));
        assertEquals(1_200, BuffGuItem.nextDuration(600, 3, 1_200, 4, true));
        assertEquals(1_200, BuffGuItem.nextDuration(1_200, 4, 600, 3, true));
    }
}

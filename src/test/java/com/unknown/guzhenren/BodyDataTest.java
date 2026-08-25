package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.Race;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BodyDataTest {

    @Test
    @DisplayName("leaving a half-zombie form clears its tier but keeps the relapse window")
    void leavingZombieFormClearsTierOnly() {
        BodyData body = new BodyData(LifeForm.HALF_ZOMBIE, Race.HUMAN, 1L, 2L, 3L, 4L, 5L, 6, 7L);

        BodyData alive = body.withLifeForm(LifeForm.ALIVE);

        assertEquals(LifeForm.ALIVE, alive.lifeForm());
        assertEquals(BodyData.NO_ZOMBIE_TIER, alive.zombieTier());
        assertEquals(5L, alive.halfZombieEndTick());
    }

    @Test
    @DisplayName("revival clears both zombie state fields")
    void revivalClearsZombieState() {
        BodyData body = new BodyData(LifeForm.ZOMBIE, Race.HUMAN, 1L, 2L, 3L, 4L, 5L, 6, 7L);

        BodyData revived = body.revived();

        assertEquals(LifeForm.ALIVE, revived.lifeForm());
        assertEquals(BodyData.UNTRACKED, revived.halfZombieEndTick());
        assertEquals(BodyData.NO_ZOMBIE_TIER, revived.zombieTier());
    }
}

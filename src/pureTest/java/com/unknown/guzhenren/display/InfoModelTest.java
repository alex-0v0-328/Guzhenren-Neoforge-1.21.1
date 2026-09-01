package com.unknown.guzhenren.display;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InfoModelTest {

    @Test
    void attackRowShowsForNonStrengthAttackBonus() {
        assertTrue(InfoModel.shouldShowAttackRow(true, 5.0D));
        assertFalse(InfoModel.shouldShowAttackRow(true, 0.0D));
        assertTrue(InfoModel.shouldShowAttackRow(false, 0.0D));
    }
}

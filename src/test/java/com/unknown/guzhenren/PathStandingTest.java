package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.display.ModDisplayText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PathStandingTest {

    private static final GuAttainment SOME = GuAttainment.GREAT_GRANDMASTER;
    private static final GuAttainment BARE = GuAttainment.NONE;

    @Test
    @DisplayName("bare only when the three values are all empty -- the 气 a player holds is a separate ask")
    void emptyOnlyWhenNothingIsThere() {
        assertTrue(ModDisplayText.pathStandingEmpty(BARE, 0L, 0L));
    }

    @Test
    @DisplayName("a count alone fills the line -- neither 气道 nor 智道 owns a tag, so the count is the content")
    void anyCountFillsTheLine() {
        assertFalse(ModDisplayText.pathStandingEmpty(BARE, 1000L, 0L));
        assertFalse(ModDisplayText.pathStandingEmpty(BARE, 0L, 500L));
        assertFalse(ModDisplayText.pathStandingEmpty(BARE, 1000L, 500L));
    }

    @Test
    @DisplayName("a grade alone fills the line too, with or without counts beside it")
    void anyGradeFillsTheLine() {
        assertFalse(ModDisplayText.pathStandingEmpty(SOME, 0L, 0L));
        assertFalse(ModDisplayText.pathStandingEmpty(SOME, 1000L, 500L));
    }

    @Test
    @DisplayName("☠ a negative count is not content -- it must not smuggle the section into being drawn")
    void negativeCountsAreStillEmpty() {
        assertTrue(ModDisplayText.pathStandingEmpty(BARE, -1L, 0L));
        assertTrue(ModDisplayText.pathStandingEmpty(BARE, 0L, -1L));
    }

    @Test
    @DisplayName("every grade above NONE fills the line, so the rule turns on NONE alone")
    void everyGradeAboveNoneFills() {
        for (GuAttainment grade : GuAttainment.values()) {
            if (grade == GuAttainment.NONE) continue;
            assertFalse(ModDisplayText.pathStandingEmpty(grade, 0L, 0L), grade.toString());
        }
    }
}

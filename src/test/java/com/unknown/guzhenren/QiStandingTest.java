package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.display.ModDisplayText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QiStandingTest {

    private static final GuAttainment SOME = GuAttainment.GREAT_GRANDMASTER;
    private static final GuAttainment BARE = GuAttainment.NONE;

    @Test
    @DisplayName("[无] only when the header's own three values are all empty")
    void emptyOnlyWhenNothingIsThere() {
        assertTrue(ModDisplayText.qiStandingEmpty(BARE, 0L, 0L));
    }

    @Test
    @DisplayName("a count alone replaces [无] -- 气道 carries no tag, so an untagged count is the content")
    void anyCountFillsTheLine() {
        assertFalse(ModDisplayText.qiStandingEmpty(BARE, 1000L, 0L));
        assertFalse(ModDisplayText.qiStandingEmpty(BARE, 0L, 500L));
        assertFalse(ModDisplayText.qiStandingEmpty(BARE, 1000L, 500L));
    }

    @Test
    @DisplayName("a grade alone replaces [无] too, with or without counts beside it")
    void anyGradeFillsTheLine() {
        assertFalse(ModDisplayText.qiStandingEmpty(SOME, 0L, 0L));
        assertFalse(ModDisplayText.qiStandingEmpty(SOME, 1000L, 500L));
    }

    @Test
    @DisplayName("☠ a negative count is not content -- it must not smuggle the line out of [无]")
    void negativeCountsAreStillEmpty() {
        assertTrue(ModDisplayText.qiStandingEmpty(BARE, -1L, 0L));
        assertTrue(ModDisplayText.qiStandingEmpty(BARE, 0L, -1L));
    }

    @Test
    @DisplayName("every grade above NONE fills the line, so the rule turns on NONE alone")
    void everyGradeAboveNoneFills() {
        for (GuAttainment grade : GuAttainment.values()) {
            if (grade == GuAttainment.NONE) continue;
            assertFalse(ModDisplayText.qiStandingEmpty(grade, 0L, 0L), grade.toString());
        }
    }
}

package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarkTagTest {

    @Test
    @DisplayName("RACE is settable on NO path -- a command must never be able to forge a race mark")
    void raceIsNeverSettable() {
        for (GuPath path : GuPath.values()) {
            assertFalse(List.of(MarkTag.settableOn(path)).contains(MarkTag.RACE), "path " + path);
        }
        assertFalse(MarkTag.RACE.isSettable());
    }

    @Test
    @DisplayName("RACE still FITS every path -- the storage layer must keep booking what a race grants")
    void raceStillFitsEveryPath() {
        for (GuPath path : GuPath.values()) {
            assertTrue(MarkTag.RACE.fitsOn(path), "path " + path);
        }
    }

    @Test
    @DisplayName("a path with no tags of its own is offered 自然 [natural] alone")
    void untaggedPathOffersNaturalOnly() {
        assertArrayEquals(new MarkTag[] {MarkTag.NATURAL}, MarkTag.settableOn(GuPath.QI));
        assertArrayEquals(new MarkTag[] {MarkTag.NATURAL}, MarkTag.settableOn(GuPath.EARTH));
    }

    @Test
    @DisplayName("力道 [STRENGTH] is the one path that owns tags, and it is offered all four plus 自然")
    void strengthOffersItsOwnFour() {
        assertArrayEquals(new MarkTag[] {
                MarkTag.NATURAL,
                MarkTag.STRENGTH_BEASTS,
                MarkTag.STRENGTH_BOAR,
                MarkTag.STRENGTH_BEAR,
                MarkTag.STRENGTH_HUMAN,
        }, MarkTag.settableOn(GuPath.STRENGTH));
    }

    @Test
    @DisplayName("STRENGTH is the only path owning tags -- every other path gets exactly one offer")
    void onlyStrengthOwnsTags() {
        int owning = 0;
        for (GuPath path : GuPath.values()) {
            int offered = MarkTag.settableOn(path).length;
            if (offered > 1) owning++;
            else assertEquals(1, offered, "path " + path);
        }
        assertEquals(1, owning);
    }

    @Test
    @DisplayName("what is offered is always a subset of what fits -- the two filters cannot disagree")
    void offeredIsSubsetOfFitting() {
        for (GuPath path : GuPath.values()) {
            for (MarkTag tag : MarkTag.settableOn(path)) {
                assertTrue(tag.fitsOn(path), tag + " on " + path);
                assertTrue(tag.isSettable(), tag.toString());
            }
        }
    }

    @Test
    @DisplayName("every tag but RACE is settable, so the exception stays a deliberate list of one")
    void raceIsTheOnlyException() {
        List<MarkTag> unsettable = Arrays.stream(MarkTag.values()).filter(tag -> !tag.isSettable()).toList();
        assertEquals(List.of(MarkTag.RACE), unsettable);
    }
}

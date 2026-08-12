package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarkTagTest {

    @Test
    @DisplayName("自然 [NATURAL] owns no path -- it is the one tag a command may book, on all 33")
    void naturalFitsEveryPath() {
        assertNull(MarkTag.NATURAL.owner());
        for (GuPath path : GuPath.values()) {
            assertTrue(MarkTag.NATURAL.fitsOn(path), "path " + path);
        }
    }

    @Test
    @DisplayName("RACE still FITS every path -- the storage layer must keep booking what a race grants")
    void raceStillFitsEveryPath() {
        for (GuPath path : GuPath.values()) {
            assertTrue(MarkTag.RACE.fitsOn(path), "path " + path);
        }
    }

    @Test
    @DisplayName("力道 [STRENGTH] is the only path owning tags, and it owns exactly four")
    void strengthIsTheOnlyOwner() {
        List<MarkTag> owned = Arrays.stream(MarkTag.values()).filter(tag -> tag.owner() != null).toList();
        assertEquals(List.of(
                MarkTag.STRENGTH_BEASTS,
                MarkTag.STRENGTH_BOAR,
                MarkTag.STRENGTH_BEAR,
                MarkTag.STRENGTH_HUMAN), owned);
        owned.forEach(tag -> assertEquals(GuPath.STRENGTH, tag.owner(), tag.toString()));
    }

    @Test
    @DisplayName("an owned tag fits its own path and NO other -- a write elsewhere is dropped in silence")
    void anOwnedTagFitsItsOwnPathAlone() {
        for (MarkTag tag : MarkTag.values()) {
            if (tag.owner() == null) continue;
            for (GuPath path : GuPath.values()) {
                assertEquals(tag.owner() == path, tag.fitsOn(path), tag + " on " + path);
            }
        }
    }

    @Test
    @DisplayName("the two boars share one tag and the bear has its own -- three constants, two species")
    void boarsShareOneSpeciesTag() {
        assertEquals(MarkTag.STRENGTH_BEASTS, MarkTag.STRENGTH_BOAR.parent());
        assertEquals(MarkTag.STRENGTH_BEASTS, MarkTag.STRENGTH_BEAR.parent());
        assertNull(MarkTag.STRENGTH_BEASTS.parent());
        assertNull(MarkTag.NATURAL.parent());
    }
}

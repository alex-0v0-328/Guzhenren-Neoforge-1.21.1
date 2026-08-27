package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureStatus;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.item.gu.mortal.earth.StoneApertureGuItem;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecondApertureTest {

    private static Aperture pool(long current, long distilled) {
        return new Aperture(Rank.THREE, Stage.INIT, 80, ExtremePhysique.NONE, current, null, null, distilled,
                0, 0L, 0, false, false, false);
    }

    @Test
    @DisplayName("the second aperture lands Grade-A at 8/10, first stage, full pool, no physique")
    void secondaryOpenedShape() {
        Aperture opened = Aperture.secondaryOpened(Rank.THREE, true);
        assertEquals(Rank.THREE, opened.rank());
        assertEquals(Stage.INIT, opened.stage());
        assertEquals(Aperture.SECONDARY_BASE, opened.baseEssence());
        assertSame(Talent.FIRST, opened.talent());
        assertEquals(opened.maxEssence(), opened.currentEssence());
        assertEquals(0L, opened.distilledEssence());
        assertEquals(ExtremePhysique.NONE, opened.extremePhysique());
        assertFalse(opened.isExtreme());
        assertTrue(opened.zombieOpened());
        assertFalse(Aperture.secondaryOpened(Rank.FIVE, false).zombieOpened());
    }

    @Test
    @DisplayName("spending walks PRIMARY distilled, then PRIMARY current, then the second aperture")
    void cascadeWalksPrimaryFirst() {
        long[][] plan = EssenceService.cascadeTake(50L, List.of(pool(100L, 10L), pool(200L, 20L)));
        assertArrayEquals(new long[] {10L, 30L}, plan[0]);
        assertArrayEquals(new long[] {0L, 0L}, plan[1]);
    }

    @Test
    @DisplayName("the distilled half rounds UP -- the last distilled point cannot pay for itself twice")
    void distilledRoundsUp() {
        long[][] plan = EssenceService.cascadeTake(5L, List.of(pool(100L, 10L)));
        assertArrayEquals(new long[] {3L, 0L}, plan[0]);
    }

    @Test
    @DisplayName("an exhausted PRIMARY spills the remainder into the second aperture")
    void cascadeSpillsIntoSecond() {
        long[][] plan = EssenceService.cascadeTake(130L, List.of(pool(0L, 0L), pool(100L, 20L)));
        assertArrayEquals(new long[] {0L, 0L}, plan[0]);
        assertArrayEquals(new long[] {20L, 90L}, plan[1]);
    }

    @Test
    @DisplayName("a single pool that covers everything never wakes the second aperture")
    void cascadeStopsWhenCovered() {
        long[][] plan = EssenceService.cascadeTake(30L, List.of(pool(100L, 0L), pool(100L, 0L)));
        assertArrayEquals(new long[] {0L, 30L}, plan[0]);
        assertArrayEquals(new long[] {0L, 0L}, plan[1]);
    }

    @Test
    @DisplayName("石窍蛊 target: PRIMARY wins while NORMAL; only DEAD or STONE passes it on")
    void stoneTargetTable() {
        assertSame(ApertureData.PRIMARY, StoneApertureGuItem.stoneTarget(
                ApertureStatus.NORMAL, ApertureStatus.NORMAL));
        assertSame(ApertureData.SECONDARY, StoneApertureGuItem.stoneTarget(
                ApertureStatus.DEAD, ApertureStatus.NORMAL));
        assertSame(ApertureData.SECONDARY, StoneApertureGuItem.stoneTarget(
                ApertureStatus.STONE, ApertureStatus.NORMAL));
        assertSame(StoneApertureGuItem.NO_TARGET, StoneApertureGuItem.stoneTarget(
                ApertureStatus.DEAD, ApertureStatus.STONE));
        assertSame(StoneApertureGuItem.NO_TARGET, StoneApertureGuItem.stoneTarget(
                ApertureStatus.DEAD, ApertureStatus.DEAD));
    }
}

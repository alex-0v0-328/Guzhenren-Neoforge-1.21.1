package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureEssenceService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureStatus;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.item.gu.mortal.earth.StoneApertureGuItem;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecondApertureTest {

    @Test
    void apertureStatusHasOnlyNormalAndDead() {
        assertEquals(2, ApertureStatus.values().length);
        assertFalse(java.util.Arrays.stream(ApertureStatus.values()).anyMatch(value -> value.name().equals("STONE")));
    }
    private static Aperture pool(long current, long distilled) {
        return new Aperture(Rank.THREE, Stage.INIT, 80, current, null, null, distilled, 0, 0L, 0, false, false);
    }
    @Test
    @DisplayName("the second aperture lands Grade-A at 8/10, first stage, full pool, no physique")
    void secondaryOpenedShape() {
        Aperture opened = Aperture.secondaryOpened(Rank.THREE);
        assertEquals(Rank.THREE, opened.rank());
        assertEquals(Stage.INIT, opened.stage());
        assertEquals(Aperture.SECONDARY_BASE, opened.baseEssence());
        assertSame(Talent.FIRST, opened.talent());
        assertEquals(opened.maxEssence(), opened.currentEssence());
        assertEquals(0L, opened.distilledEssence());
        assertTrue(opened.second());
    }
    @Test
    @DisplayName("a lone second aperture is not awakened; Hope Gu inserts the first one ahead of it")
    void awakeningOrderAndInsertFirst() {
        Aperture second = Aperture.secondaryOpened(Rank.THREE);
        ApertureData lone = new ApertureData(List.of(second));
        assertTrue(lone.hasAperture());
        assertFalse(lone.isAwakened());
        assertEquals(0, lone.secondIndex());
        assertEquals(-1, lone.firstIndex());

        ApertureData both = lone.insertFirst(Aperture.openedAt(80));
        assertTrue(both.isAwakened());
        assertEquals(0, both.firstIndex());
        assertEquals(1, both.secondIndex());
        assertSame(second, both.get(1));
        assertFalse(both.primary().second());
    }
    @Test
    @DisplayName("insertFirst falls back to append once the first aperture exists")
    void insertFirstAppendsWhenFirstExists() {
        ApertureData one = new ApertureData(List.of(Aperture.openedAt(80)));
        ApertureData two = one.insertFirst(Aperture.secondaryOpened(Rank.THREE));
        assertFalse(two.get(0).second());
        assertEquals(1, two.secondIndex());
    }
    @Test
    @DisplayName("decoding heals a pre-flag save: the entry at position 1 becomes the second aperture")
    void codecHealsLegacyPositions() {
        JsonElement legacy = Aperture.CODEC.encodeStart(JsonOps.INSTANCE, Aperture.secondaryOpened(Rank.THREE))
                .getOrThrow();
        ((JsonObject) legacy).remove("second");

        JsonArray list = new JsonArray();
        list.add(Aperture.CODEC.encodeStart(JsonOps.INSTANCE, Aperture.openedAt(80)).getOrThrow());
        list.add(legacy);

        ApertureData healed = ApertureData.CODEC.parse(JsonOps.INSTANCE, list).getOrThrow();
        assertFalse(healed.get(0).second());
        assertTrue(healed.get(1).second());
    }
    @Test
    @DisplayName("spending walks PRIMARY distilled, then PRIMARY current, then the second aperture")
    void cascadeWalksPrimaryFirst() {
        long[][] plan = ApertureEssenceService.cascadeTake(50L, List.of(pool(100L, 10L), pool(200L, 20L)));
        assertArrayEquals(new long[] {10L, 30L}, plan[0]);
        assertArrayEquals(new long[] {0L, 0L}, plan[1]);
    }
    @Test
    @DisplayName("the distilled half rounds UP -- the last distilled point cannot pay for itself twice")
    void distilledRoundsUp() {
        long[][] plan = ApertureEssenceService.cascadeTake(5L, List.of(pool(100L, 10L)));
        assertArrayEquals(new long[] {3L, 0L}, plan[0]);
    }
    @Test
    @DisplayName("an exhausted PRIMARY spills the remainder into the second aperture")
    void cascadeSpillsIntoSecond() {
        long[][] plan = ApertureEssenceService.cascadeTake(130L, List.of(pool(0L, 0L), pool(100L, 20L)));
        assertArrayEquals(new long[] {0L, 0L}, plan[0]);
        assertArrayEquals(new long[] {20L, 90L}, plan[1]);
    }
    @Test
    @DisplayName("a single pool that covers everything never wakes the second aperture")
    void cascadeStopsWhenCovered() {
        long[][] plan = ApertureEssenceService.cascadeTake(30L, List.of(pool(100L, 0L), pool(100L, 0L)));
        assertArrayEquals(new long[] {0L, 30L}, plan[0]);
        assertArrayEquals(new long[] {0L, 0L}, plan[1]);
    }
    @Test
    @DisplayName("石窍蛊 target: PRIMARY wins while NORMAL; only DEAD passes it on")
    void stoneTargetTable() {
        assertSame(ApertureData.PRIMARY, StoneApertureGuItem.stoneTarget(
                ApertureStatus.NORMAL, ApertureStatus.NORMAL));
        assertSame(ApertureData.SECONDARY, StoneApertureGuItem.stoneTarget(
                ApertureStatus.DEAD, ApertureStatus.NORMAL));
        assertSame(StoneApertureGuItem.NO_TARGET, StoneApertureGuItem.stoneTarget(
                ApertureStatus.DEAD, ApertureStatus.DEAD));
    }
}

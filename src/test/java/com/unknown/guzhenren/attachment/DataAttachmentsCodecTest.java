package com.unknown.guzhenren.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureNourishData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.data.path.PathData;
import com.unknown.guzhenren.attachment.data.path.PathEntry;
import com.unknown.guzhenren.attachment.data.path.PathQiData;
import com.unknown.guzhenren.attachment.data.path.PathQiEntry;
import com.unknown.guzhenren.attachment.data.path.PathStrengthData;
import com.unknown.guzhenren.attachment.data.soul.SoulData;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Stream-codec roundtrips for the record attachments DataStreamCodecTest does not cover.
 *
 * <p>Each test encodes a pinned instance, decodes field by field with independently rebuilt codecs,
 * re-encodes, and asserts whole-record equality plus a drained buffer. ApertureStorage is serialized
 * but never synced and has no stream codec, so it roundtrips through its NBT CODEC on plain NbtOps.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

class DataAttachmentsCodecTest {

    @Test
    @DisplayName("ApertureNourishData stream codec preserves the running flag, target and starve anchor")
    void nourishRoundTrip() {
        ApertureNourishData expected = new ApertureNourishData(true, 1, 12_345L);
        ByteBuf buffer = Unpooled.buffer();
        try {
            ApertureNourishData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.cultivating(), ByteBufCodecs.BOOL.decode(buffer));
            assertEquals(expected.target(), ByteBufCodecs.VAR_INT.decode(buffer));
            assertEquals(expected.starvedSinceTick(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            ApertureNourishData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, ApertureNourishData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            ApertureNourishData.STREAM_CODEC.encode(buffer,
                    new ApertureNourishData(false, ApertureData.PRIMARY, ApertureNourishData.NOT_STARVED));
            assertFalse(ByteBufCodecs.BOOL.decode(buffer));
            assertEquals(0, ByteBufCodecs.VAR_INT.decode(buffer));
            assertEquals(-1L, ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("SoulData stream codec roundtrips and the compact ctor clamps current into [0, max]")
    void soulRoundTrip() {
        SoulData expected = new SoulData(50L, 999L);
        assertEquals(50L, expected.currentSoul());
        ByteBuf buffer = Unpooled.buffer();
        try {
            SoulData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.maxSoul(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(expected.currentSoul(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            SoulData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, SoulData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            SoulData collapsed = new SoulData(0L, 0L);
            SoulData.STREAM_CODEC.encode(buffer, collapsed);
            assertEquals(collapsed, SoulData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("PathData stream codec roundtrips entries and keeps non-default ones only")
    void pathDataRoundTrip() {
        PathData expected = new PathData(Map.of(
                GuPath.TIME, new PathEntry(GuAttainment.GRANDMASTER, Map.of(MarkTag.EXTREME_PHYSIQUE, 1000L)),
                GuPath.WISDOM, new PathEntry(GuAttainment.QUASI_MASTER, Map.of())));
        ByteBuf buffer = Unpooled.buffer();
        try {
            PathData.STREAM_CODEC.encode(buffer, expected);
            Map<GuPath, PathEntry> decoded = ModStreamCodecs.enumMap(GuPath.class, PathEntry.STREAM_CODEC).decode(buffer);
            assertEquals(expected.entries(), decoded);
            assertEquals(2, decoded.size());
            assertEquals(1000L, decoded.get(GuPath.TIME).markTotal());
            assertEquals(GuAttainment.QUASI_MASTER, decoded.get(GuPath.WISDOM).attainment());
            assertEquals(0, buffer.readableBytes());
            PathData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, PathData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("PathStrengthData stream codec roundtrips the beast set and capped human layers")
    void pathStrengthRoundTrip() {
        PathStrengthData expected = new PathStrengthData(Set.of(BeastStrength.WHITE_BOAR, BeastStrength.BEAR),
                Map.of(HumanStrength.JIN, 9, HumanStrength.TEN_JUN, 30));
        ByteBuf buffer = Unpooled.buffer();
        try {
            PathStrengthData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.beasts(), ModStreamCodecs.enumSet(BeastStrength.class).decode(buffer));
            assertEquals(expected.humanStrength(),
                    ModStreamCodecs.enumMap(HumanStrength.class, ByteBufCodecs.VAR_INT).decode(buffer));
            assertEquals(0, buffer.readableBytes());
            PathStrengthData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, PathStrengthData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            PathStrengthData clamped = new PathStrengthData(Set.of(), Map.of(HumanStrength.JIN, 999));
            assertEquals(9, clamped.humanStrengthCount(HumanStrength.JIN));
            PathStrengthData.STREAM_CODEC.encode(buffer, clamped);
            assertEquals(clamped, PathStrengthData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("PathQiEntry and PathQiData stream codecs roundtrip, zero hold end included")
    void pathQiRoundTrip() {
        PathQiEntry expected = new PathQiEntry(640L, 0L);
        ByteBuf buffer = Unpooled.buffer();
        try {
            PathQiEntry.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.amount(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(0L, ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            PathQiData full = new PathQiData(Map.of(
                    QiKind.STRENGTH, new PathQiEntry(64L, 24_000L),
                    QiKind.LIFE, new PathQiEntry(640L, 3_600L),
                    QiKind.ESSENCE, new PathQiEntry(128L, 0L)));
            PathQiData.STREAM_CODEC.encode(buffer, full);
            Map<QiKind, PathQiEntry> decoded =
                    ModStreamCodecs.enumMap(QiKind.class, PathQiEntry.STREAM_CODEC).decode(buffer);
            assertEquals(full.entries(), decoded);
            assertEquals(0L, decoded.get(QiKind.ESSENCE).holdEndTick());
            assertEquals(0, buffer.readableBytes());
            PathQiData.STREAM_CODEC.encode(buffer, full);
            assertEquals(full, PathQiData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("MindPool stream codec roundtrips and latches the overflow flag")
    void mindPoolRoundTrip() {
        MindPool expected = new MindPool(30L, 50_000L, false);
        ByteBuf buffer = Unpooled.buffer();
        try {
            MindPool.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.current(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(expected.max(), ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(expected.bufferUsed(), ByteBufCodecs.BOOL.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            MindPool.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, MindPool.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            MindPool overflowing = new MindPool(65_000L, 50_000L, false);
            assertTrue(overflowing.bufferUsed());
            MindPool.STREAM_CODEC.encode(buffer, overflowing);
            assertEquals(65_000L, ByteBufCodecs.VAR_LONG.decode(buffer));
            assertEquals(50_000L, ByteBufCodecs.VAR_LONG.decode(buffer));
            assertTrue(ByteBufCodecs.BOOL.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("MindData stream codec roundtrips brilliance, dense pools and tagged thoughts")
    void mindRoundTrip() {
        MindData expected = new MindData(Brilliance.OUTSTANDING,
                Map.of(WisdomType.THOUGHTS, new MindPool(100L, 50_000L, false)),
                Map.of(ThoughtTag.EVIL, 30L));
        ByteBuf buffer = Unpooled.buffer();
        try {
            MindData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected.brilliance(), ModStreamCodecs.ofEnum(Brilliance.class).decode(buffer));
            assertEquals(expected.pools(),
                    ModStreamCodecs.enumMap(WisdomType.class, MindPool.STREAM_CODEC).decode(buffer));
            assertEquals(expected.taggedThoughts(),
                    ModStreamCodecs.enumMap(ThoughtTag.class, ByteBufCodecs.VAR_LONG).decode(buffer));
            assertEquals(0, buffer.readableBytes());
            MindData.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, MindData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
            assertEquals(3, expected.pools().size());
            assertEquals(new MindPool(0L, 12L, false), expected.pool(WisdomType.WILLS));
        } finally {
            buffer.release();
        }
    }

    @Test
    @DisplayName("ApertureStorage NBT codec keeps interior holes and the vital slot")
    void apertureStorageKeepsInteriorHoles() {
        ApertureStorage expected = new ApertureStorage(
                List.of(List.of(new ItemStack(Items.DIRT, 3), ItemStack.EMPTY, new ItemStack(Items.STONE)),
                        List.of(new ItemStack(Items.APPLE, 2))),
                List.of(new ItemStack(Items.DIRT)));
        ApertureStorage decoded = nbtRoundTrip(expected);
        assertSameContents(expected, decoded);
        assertEquals(2, decoded.byAperture().size());
        assertEquals(3, decoded.get(0).size());
        assertTrue(decoded.get(0).get(1).isEmpty());
        assertEquals(1, decoded.get(1).size());
        assertEquals(2, decoded.get(1).getFirst().getCount());
        assertEquals(1, decoded.getVital(0).getCount());
    }

    @Test
    @DisplayName("ApertureStorage NBT codec trims trailing holes only")
    void apertureStorageTrimsTrailingHoles() {
        ApertureStorage trimmed = new ApertureStorage(
                List.of(List.of(new ItemStack(Items.DIRT)), List.of(), List.of(ItemStack.EMPTY)), List.of());
        assertEquals(1, trimmed.byAperture().size());
        ApertureStorage decoded = nbtRoundTrip(trimmed);
        assertSameContents(trimmed, decoded);
        assertEquals(1, decoded.byAperture().size());
    }

    @Test
    @DisplayName("every attachment default roundtrips through its own codec")
    void defaultsRoundTrip() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            ApertureNourishData.STREAM_CODEC.encode(buffer, ApertureNourishData.DEFAULT);
            assertEquals(ApertureNourishData.DEFAULT, ApertureNourishData.STREAM_CODEC.decode(buffer));
            SoulData.STREAM_CODEC.encode(buffer, SoulData.DEFAULT);
            assertEquals(SoulData.DEFAULT, SoulData.STREAM_CODEC.decode(buffer));
            PathData.STREAM_CODEC.encode(buffer, PathData.DEFAULT);
            assertEquals(PathData.DEFAULT, PathData.STREAM_CODEC.decode(buffer));
            PathStrengthData.STREAM_CODEC.encode(buffer, PathStrengthData.DEFAULT);
            assertEquals(PathStrengthData.DEFAULT, PathStrengthData.STREAM_CODEC.decode(buffer));
            PathQiData.STREAM_CODEC.encode(buffer, PathQiData.DEFAULT);
            assertEquals(PathQiData.DEFAULT, PathQiData.STREAM_CODEC.decode(buffer));
            MindData.STREAM_CODEC.encode(buffer, MindData.DEFAULT);
            assertEquals(MindData.DEFAULT, MindData.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
        assertEquals(ApertureStorage.DEFAULT, nbtRoundTrip(ApertureStorage.DEFAULT));
    }

    private static ApertureStorage nbtRoundTrip(ApertureStorage storage) {
        Tag tag = ApertureStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage).getOrThrow();
        return ApertureStorage.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    private static void assertSameContents(ApertureStorage expected, ApertureStorage actual) {
        assertEquals(expected.byAperture().size(), actual.byAperture().size());
        for (int i = 0; i < expected.byAperture().size(); i++) {
            int aperture = i;
            List<ItemStack> wanted = expected.byAperture().get(i);
            List<ItemStack> stored = actual.byAperture().get(i);
            assertEquals(wanted.size(), stored.size(), () -> "page size differs at aperture " + aperture);
            for (int j = 0; j < wanted.size(); j++) {
                int slot = j;
                assertTrue(ItemStack.matches(wanted.get(j), stored.get(j)),
                        () -> "stack differs at aperture " + aperture + " slot " + slot);
            }
        }
        assertEquals(expected.vital().size(), actual.vital().size());
        for (int i = 0; i < expected.vital().size(); i++) {
            int aperture = i;
            assertTrue(ItemStack.matches(expected.vital().get(i), actual.vital().get(i)),
                    () -> "vital stack differs at aperture " + aperture);
        }
    }
}

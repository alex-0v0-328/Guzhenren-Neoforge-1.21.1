package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PathEntry(GuAttainment attainment, Map<MarkTag, Long> marks, Map<MarkTag, Long> specks) {

    public static final PathEntry DEFAULT = new PathEntry(GuAttainment.NONE, Map.of(), Map.of());

    //     TODO(convert): mark <-> speck at 1:10000 -- no caller yet; it needs a Gu or item to trigger it.
    public static final long MARK_PER_SPECK = 10_000L;

    private static final Codec<Map<MarkTag, Long>> TAGS = Codec.unboundedMap(MarkTag.CODEC, Codec.LONG);

    public static final Codec<PathEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuAttainment.CODEC.optionalFieldOf("attainment", GuAttainment.NONE).forGetter(PathEntry::attainment),
            TAGS.optionalFieldOf("marks", Map.of()).forGetter(PathEntry::marks),
            TAGS.optionalFieldOf("specks", Map.of()).forGetter(PathEntry::specks)
    ).apply(instance, PathEntry::new));

    public static final StreamCodec<ByteBuf, PathEntry> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuAttainment.class), PathEntry::attainment,
            ModStreamCodecs.enumMap(MarkTag.class, ByteBufCodecs.VAR_LONG), PathEntry::marks,
            ModStreamCodecs.enumMap(MarkTag.class, ByteBufCodecs.VAR_LONG), PathEntry::specks,
            PathEntry::new);

    public PathEntry {
        marks = normalized(marks);
        specks = normalized(specks);
    }

    public long mark() {return sum(marks);}
    public long speck() {return sum(specks);}
    public long mark(MarkTag tag) {return marks.getOrDefault(tag, 0L);}
    public long speck(MarkTag tag) {return specks.getOrDefault(tag, 0L);}
    public PathEntry withAttainment(GuAttainment v) {return new PathEntry(v, marks, specks);}
    public PathEntry withMark(MarkTag t, long v) {return new PathEntry(attainment, set(marks, t, v), specks);}
    public PathEntry withSpeck(MarkTag t, long v) {return new PathEntry(attainment, marks, set(specks, t, v));}

    public boolean isDefault() {return attainment == GuAttainment.NONE && marks.isEmpty() && specks.isEmpty();}

    public PathEntry retainingTagsFor(GuPath path) {
        Map<MarkTag, Long> keptMarks = fitting(marks, path);
        Map<MarkTag, Long> keptSpecks = fitting(specks, path);
        return keptMarks == marks && keptSpecks == specks ? this : new PathEntry(attainment, keptMarks, keptSpecks);
    }

    private static long sum(Map<MarkTag, Long> tags) {
        long total = 0L;
        for (long value : tags.values()) total += value;
        return total;
    }

    private static Map<MarkTag, Long> normalized(Map<MarkTag, Long> tags) {
        Map<MarkTag, Long> pruned = new EnumMap<>(MarkTag.class);
        tags.forEach((tag, value) -> {
            if (value != null && value > 0L) pruned.put(tag, value);
        });
        return Collections.unmodifiableMap(pruned);
    }

    private static Map<MarkTag, Long> set(Map<MarkTag, Long> tags, MarkTag tag, long value) {
        Map<MarkTag, Long> next = new EnumMap<>(MarkTag.class);
        next.putAll(tags);
        next.put(tag, Math.max(0L, value));
        return next;
    }

    private static Map<MarkTag, Long> fitting(Map<MarkTag, Long> tags, GuPath path) {
        if (tags.keySet().stream().allMatch(tag -> tag.fitsOn(path))) return tags;
        Map<MarkTag, Long> kept = new EnumMap<>(MarkTag.class);
        tags.forEach((tag, value) -> {
            if (tag.fitsOn(path)) kept.put(tag, value);
        });
        return Collections.unmodifiableMap(kept);
    }
}

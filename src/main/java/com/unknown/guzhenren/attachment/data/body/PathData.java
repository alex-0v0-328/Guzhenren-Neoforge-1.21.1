package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

//  The path [流派] system. Sparse -- defaults pruned, an absent key reads as the default (get() never null).
//  ⚠ A featured path's MARK is not here (its sub-system's total). Its speck is ordinary and lives here.
public record PathData(Map<GuPath, PathEntry> entries) {

    public static final PathData DEFAULT = new PathData(Map.of());

    public static final Codec<PathData> CODEC = Codec.unboundedMap(GuPath.CODEC, PathEntry.CODEC)
            .xmap(PathData::new, PathData::entries);

    public static final StreamCodec<ByteBuf, PathData> STREAM_CODEC =
            ModStreamCodecs.enumMap(GuPath.class, PathEntry.STREAM_CODEC).map(PathData::new, PathData::entries);

    public PathData {
        //  EnumMap: stable ordinal order in NBT and on the wire.
        //  ⚠ Only the MARK of a featured path is zeroed -- a stored copy could only disagree with the
        //  sub-system's total. Its SPECK is an ordinary stored count; featured says nothing about specks.
        Map<GuPath, PathEntry> pruned = new EnumMap<>(GuPath.class);
        entries.forEach((path, entry) -> {
            PathEntry kept = path.isFeatured() ? entry.withMark(0L) : entry;
            if (!kept.isDefault()) pruned.put(path, kept);
        });
        entries = Collections.unmodifiableMap(pruned);
    }

    public PathEntry get(GuPath path) {return entries.getOrDefault(path, PathEntry.DEFAULT);}

    public PathData with(GuPath path, PathEntry entry) {
        Map<GuPath, PathEntry> next = new EnumMap<>(GuPath.class);
        next.putAll(entries);
        next.put(path, entry);
        return new PathData(next);
    }
}

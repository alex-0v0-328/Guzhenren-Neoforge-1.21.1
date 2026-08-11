package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

/**
 * Path [流派] progress, sparse: a path missing from the map is simply one nobody has walked.
 *
 * <p>⚠ The compact constructor is the one door -- a MarkTag owned by a different path is dropped there,
 * so no writer can file a foreign tag under a path.
 *
 * @author Alex
 * @since 1.0.0
 * @see PathEntry
 */
public record PathData(Map<GuPath, PathEntry> entries) {

    public static final PathData DEFAULT = new PathData(Map.of());

    public static final Codec<PathData> CODEC = Codec.unboundedMap(GuPath.CODEC, PathEntry.CODEC)
            .xmap(PathData::new, PathData::entries);

    public static final StreamCodec<ByteBuf, PathData> STREAM_CODEC =
            ModStreamCodecs.enumMap(GuPath.class, PathEntry.STREAM_CODEC).map(PathData::new, PathData::entries);

    public PathData {
        Map<GuPath, PathEntry> pruned = new EnumMap<>(GuPath.class);
        entries.forEach((path, entry) -> {
            PathEntry kept = entry.retainingTagsFor(path);
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

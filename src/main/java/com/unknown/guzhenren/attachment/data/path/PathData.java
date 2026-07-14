package com.unknown.guzhenren.attachment.data.path;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.util.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The path (流派) system. Sparse: a cultivator dabbles in two or three of the 30, so default entries
//  are pruned and an absent key reads back as the default -- get() never returns null.
public record PathData(Map<GuPath, PathEntry> entries) {

    public static final PathData DEFAULT = new PathData(Map.of());

    public static final Codec<PathData> CODEC = Codec.unboundedMap(GuPath.CODEC, PathEntry.CODEC)
            .xmap(PathData::new, PathData::entries);

    public static final StreamCodec<ByteBuf, PathData> STREAM_CODEC = ByteBufCodecs
            .<ByteBuf, GuPath, PathEntry, Map<GuPath, PathEntry>>map(
                    HashMap::new, ModStreamCodecs.ofEnum(GuPath.class), PathEntry.STREAM_CODEC)
            .map(PathData::new, PathData::entries);

    public PathData {
        //  EnumMap: stable ordinal order in NBT and on the wire.
        Map<GuPath, PathEntry> pruned = new EnumMap<>(GuPath.class);
        entries.forEach((path, entry) -> {
            if (!entry.isDefault()) pruned.put(path, entry);
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

package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The qi (气) system: marks per type, **no cap**, sparse -- an unearned type is absent and reads back as 0.
//  ⚠ total() IS the Qi Path's marks; they are not stored in PathData.  CLAUDE.md "Qi".
public record QiData(Map<QiType, Long> marks) {

    public static final QiData DEFAULT = new QiData(Map.of());

    public static final Codec<QiData> CODEC = Codec.unboundedMap(QiType.CODEC, Codec.LONG)
            .xmap(QiData::new, QiData::marks);

    public static final StreamCodec<ByteBuf, QiData> STREAM_CODEC =
            ModStreamCodecs.enumMap(QiType.class, ByteBufCodecs.VAR_LONG).map(QiData::new, QiData::marks);

    public QiData {
        //  EnumMap: stable ordinal order in NBT and on the wire. 0 is "absent", so it is pruned.
        Map<QiType, Long> pruned = new EnumMap<>(QiType.class);
        marks.forEach((type, mark) -> {
            if (mark != null && mark > 0L) pruned.put(type, mark);
        });
        marks = Collections.unmodifiableMap(pruned);
    }

    public long mark(QiType type) {return marks.getOrDefault(type, 0L);}

    //  The Qi Path's marks: heaven 100 + earth 50 + human 500 = 650. One fact, two views.
    public long total() {
        long sum = 0L;
        for (long mark : marks.values()) sum += mark;
        return sum;
    }

    //  EnumMap(Class), never EnumMap(Map) -- the latter throws on an empty non-EnumMap, and marks is one.
    public QiData with(QiType type, long mark) {
        Map<QiType, Long> next = new EnumMap<>(QiType.class);
        next.putAll(marks);
        next.put(type, Math.max(0L, mark));
        return new QiData(next);
    }
}

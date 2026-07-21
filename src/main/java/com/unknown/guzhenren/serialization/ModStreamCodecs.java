package com.unknown.guzhenren.serialization;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ModStreamCodecs {

    private ModStreamCodecs() {}

    //  ⚠ Enums travel as their ordinal. Safe only because client and server run the same jar.
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(ordinal -> values[ordinal], Enum::ordinal);
    }

    //  ⚠ Same ordinal trick, shifted by one so 0 can mean "unset" -- the only nullable field in the
    //  data model is a path nobody has chosen yet. See Aperture's primary / secondary path.
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofNullableEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(i -> i == 0 ? null : values[i - 1],
                value -> value == null ? 0 : value.ordinal() + 1);
    }

    //  An enum-keyed map on the wire. Hides the 4-type witness PathData/MindData would need inline.
    public static <K extends Enum<K>, V> StreamCodec<ByteBuf, Map<K, V>> enumMap(
            Class<K> key, StreamCodec<ByteBuf, V> value) {
        return ByteBufCodecs.map(HashMap::new, ofEnum(key), value);
    }

    //  An enum set on the wire. Arrives as a plain HashSet; the record's compact ctor re-normalizes it.
    public static <E extends Enum<E>> StreamCodec<ByteBuf, Set<E>> enumSet(Class<E> type) {
        return ByteBufCodecs.collection(HashSet::new, ofEnum(type));
    }
}

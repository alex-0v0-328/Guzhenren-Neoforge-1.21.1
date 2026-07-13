package com.unknown.guzhenren.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ModStreamCodecs {

    private ModStreamCodecs() {}

    //  Enums travel as their ordinal. Safe here because client and server always run the same
    //  jar; do not reuse this for anything that crosses a version boundary (saves, datapacks).
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(ordinal -> values[ordinal], Enum::ordinal);
    }
}

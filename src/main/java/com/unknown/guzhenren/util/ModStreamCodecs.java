package com.unknown.guzhenren.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ModStreamCodecs {

    private ModStreamCodecs() {}

    //  ⚠ Enums travel as their ordinal. Safe only because client and server run the same jar -- never
    //  reuse this across a version boundary (saves, datapacks).
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(ordinal -> values[ordinal], Enum::ordinal);
    }
}

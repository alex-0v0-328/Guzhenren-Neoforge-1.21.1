package com.unknown.guzhenren.serialization;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The stream codecs this mod adds: an enum as its ordinal, and enum-keyed maps and sets.
 *
 * <p>Provides {@code ofEnum} (ordinal round-trip), {@code ofNullableEnum} (ordinal + 1, zero = unset),
 * {@code enumMap}, and {@code enumSet}. The nullable-enum idiom is what lets the two nullable
 * {@code GuPath} fields on {@code Aperture} travel on the wire without a {@code NONE} constant to
 * lean on. Used by the attachment stream codecs and the client-intent payloads.
 *
 * <p>⚠ A nullable enum travels as its ordinal plus one, with zero meaning unset. No enum here has a
 * NONE constant to lean on, so this is where "has not chosen" becomes representable on the wire.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ModStreamCodecs {

    private ModStreamCodecs() {}

    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(ordinal -> values[ordinal], Enum::ordinal);
    }

    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ofNullableEnum(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(i -> i == 0 ? null : values[i - 1],
                value -> value == null ? 0 : value.ordinal() + 1);
    }

    public static <K extends Enum<K>, V> StreamCodec<ByteBuf, Map<K, V>> enumMap(
            Class<K> key, StreamCodec<ByteBuf, V> value) {
        return ByteBufCodecs.map(HashMap::new, ofEnum(key), value);
    }

    public static <E extends Enum<E>> StreamCodec<ByteBuf, Set<E>> enumSet(Class<E> type) {
        return ByteBufCodecs.collection(HashSet::new, ofEnum(type));
    }
}

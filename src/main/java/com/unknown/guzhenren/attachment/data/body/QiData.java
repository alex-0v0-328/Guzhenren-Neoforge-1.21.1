package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Qi [气] holdings, sparse: a kind missing from the map is simply one the player is not holding.
 *
 * <p>Immutable record attachment keyed {@code qi_data}; {@link
 * com.unknown.guzhenren.attachment.service.body.QiService} is the only writer. Each entry is a
 * {@link QiEntry} -- a time anchor, not a running total -- so the live amount is always derived from
 * the current tick by {@code current(kind, now)}.
 *
 * <p>⚠ Every read takes the current tick, because an entry anchors a moment rather than a balance. A
 * reader without it would report an amount that has already drained away. ⚠ {@code holding()} returns
 * false for untimed kinds (they never expire), which is why a "currently holding" check is not the same
 * as "amount > 0" for timed kinds past their hold.
 *
 * @author Alex
 * @version 1.0.0
 * @see QiEntry
 * @see com.unknown.guzhenren.attachment.service.body.QiService
 * @since 1.0.0
 */

public record QiData(Map<QiKind, QiEntry> entries) {

    public static final QiData DEFAULT = new QiData(Map.of());

    public static final Codec<QiData> CODEC = Codec.unboundedMap(QiKind.CODEC, QiEntry.CODEC)
            .xmap(QiData::new, QiData::entries);

    public static final StreamCodec<ByteBuf, QiData> STREAM_CODEC =
            ModStreamCodecs.enumMap(QiKind.class, QiEntry.STREAM_CODEC).map(QiData::new, QiData::entries);

    public QiData {
        Map<QiKind, QiEntry> copy = new EnumMap<>(QiKind.class);
        copy.putAll(entries);
        entries = Collections.unmodifiableMap(copy);
    }

    public @Nullable QiEntry get(QiKind kind) {return entries.get(kind);}

    public long current(QiKind kind, long now) {
        QiEntry entry = entries.get(kind);
        if (entry == null) return 0L;
        if (!kind.isTimed() || now < entry.holdEndTick()) return entry.amount();

        long elapsedSeconds = (now - entry.holdEndTick()) / Ticks.SECOND;
        int tier = Math.max(0, QiKind.tierOf(entry.amount()));
        long rate = QiKind.decayPerSecond(tier);
        long decaySeconds = Math.ceilDiv(entry.amount(), rate);
        if (elapsedSeconds >= decaySeconds) return 0L;
        return entry.amount() - rate * elapsedSeconds;
    }

    public boolean holding(QiKind kind, long now) {
        QiEntry entry = entries.get(kind);
        return entry != null && kind.isTimed() && now < entry.holdEndTick();
    }

    public QiData with(QiKind kind, QiEntry entry) {
        Map<QiKind, QiEntry> next = new EnumMap<>(QiKind.class);
        next.putAll(entries);
        next.put(kind, entry);
        return new QiData(next);
    }

    public QiData without(QiKind kind) {
        if (!entries.containsKey(kind)) return this;
        Map<QiKind, QiEntry> next = new EnumMap<>(QiKind.class);
        next.putAll(entries);
        next.remove(kind);
        return new QiData(next);
    }
}

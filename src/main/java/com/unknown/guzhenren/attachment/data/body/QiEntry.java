package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One kind of Qi [气] the player is currently holding.
 *
 * <p>⚠ A time anchor, not a running total. Nothing ticks it down -- what is left is derived from the
 * current tick, which is why this needs no clock of its own.
 *
 * @author Alex
 * @since 1.0.0
 */
public record QiEntry(long amount, long holdEndTick) {

    public static final Codec<QiEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("amount", 0L).forGetter(QiEntry::amount),
            Codec.LONG.optionalFieldOf("hold_end_tick", 0L).forGetter(QiEntry::holdEndTick)
    ).apply(instance, QiEntry::new));

    public static final StreamCodec<ByteBuf, QiEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, QiEntry::amount,
            ByteBufCodecs.VAR_LONG, QiEntry::holdEndTick,
            QiEntry::new);

    public QiEntry {
        amount = Math.max(0L, amount);
        holdEndTick = Math.max(0L, holdEndTick);
    }
}

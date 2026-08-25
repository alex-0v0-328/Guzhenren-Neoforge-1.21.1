package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One kind of Qi [气] the player is currently holding.
 *
 * <p>Leaf record nested inside {@link QiData}; immutable. Two components: the amount and the tick the
 * hold ends at. The live amount is derived by {@link QiData#current} from the current tick, so this
 * record carries no running counter and ticks nothing down itself.
 *
 * <p>⚠ A time anchor, not a running total -- nothing ticks it down, which is why it needs no clock of
 * its own. ⚠ Re-adding to a kind does not "take the higher grade": {@link
 * com.unknown.guzhenren.attachment.service.body.QiService#set} re-anchors the hold on the SUM, so a
 * write path that re-applies the same kind restarts the hold full. ⚠ Both fields are floored at 0 in
 * the compact ctor; {@code holdEndTick == 0} is a valid (if unusual) value.
 *
 * @author Alex
 * @version 1.0.0
 * @see QiData
 * @see com.unknown.guzhenren.attachment.service.body.QiService
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

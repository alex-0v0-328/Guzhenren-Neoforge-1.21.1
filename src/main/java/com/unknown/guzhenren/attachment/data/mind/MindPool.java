package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One cell of the Mind Ocean: 念 / 意 / 情. The map key in MindData says which.
//  current runs 0..2×max: 0..max is normal, max..2×max is the buffer (survivable), > 2×max bursts.
//  bufferUsed latches true once current passes max and clears only when a sleep brings 念 back down.
public record MindPool(long current, long max, boolean bufferUsed) {

    public static final Codec<MindPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("current", 0L).forGetter(MindPool::current),
            Codec.LONG.optionalFieldOf("max", 0L).forGetter(MindPool::max),
            Codec.BOOL.optionalFieldOf("buffer_used", false).forGetter(MindPool::bufferUsed)
    ).apply(instance, MindPool::new));

    public static final StreamCodec<ByteBuf, MindPool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, MindPool::current,
            ByteBufCodecs.VAR_LONG, MindPool::max,
            ByteBufCodecs.BOOL, MindPool::bufferUsed,
            MindPool::new);

    //  ⚠ current is never clamped to max: the buffer (max..2×max) has to be representable, and so does
    //  the burst past it. current > max forces bufferUsed on, so the flag can never lie.
    public MindPool {
        current = Math.max(0L, current);
        max = Math.max(0L, max);
        bufferUsed = bufferUsed || current > max;
    }

    public static MindPool of(GuWisdomType type) {return new MindPool(0L, type.getDefaultCapacity(), false);}

    //  脑海炸裂 past the buffer, i.e. current > 2×max. Written current-max>max to dodge a 2×max overflow.
    public boolean isOverflowing() {return current - max > max;}

    public MindPool withCurrent(long v) {return new MindPool(v, max, bufferUsed);}
    public MindPool withMax(long v) {return new MindPool(current, v, bufferUsed);}

    //  A night's sleep restores 念 toward the cap -- half the deficit if the buffer was used since the
    //  last sleep, the whole deficit otherwise. Never reduces current; the flag re-derives from the result.
    public MindPool slept() {
        long restored = bufferUsed && current < max ? current + (max - current) / 2 : Math.max(current, max);
        return new MindPool(restored, max, false);
    }

    //  Respawn brings a burst pool back to a safe, rested cap.
    public MindPool clamped() {return isOverflowing() ? new MindPool(max, max, false) : this;}
}

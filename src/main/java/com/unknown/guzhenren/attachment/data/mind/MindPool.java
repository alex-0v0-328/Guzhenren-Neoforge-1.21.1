package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One cell of the Mind Ocean: 念 / 意 / 情. The map key in MindData says which.
//  current 0..max normal, max..2×max buffer, >2×max bursts; bufferUsed latches once past max.
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

    //  ⚠ current never clamped to max -- buffer/burst must be representable; current > max forces bufferUsed on.
    public MindPool {
        current = Math.max(0L, current);
        max = Math.max(0L, max);
        bufferUsed = bufferUsed || current > max;
    }

    public static MindPool of(WisdomType type) {return new MindPool(0L, type.getDefaultCapacity(), false);}

    //  脑海炸裂 past the buffer, i.e. current > 2×max. Written current-max>max to dodge a 2×max overflow.
    public boolean isOverflowing() {return current - max > max;}

    public MindPool withCurrent(long v) {return new MindPool(v, max, bufferUsed);}
    public MindPool withMax(long v) {return new MindPool(current, v, bufferUsed);}

    //  Sleep restores 念 toward cap -- half the deficit if buffer was used, else whole. Never reduces current.
    public MindPool slept() {
        long restored = bufferUsed && current < max ? current + (max - current) / 2 : Math.max(current, max);
        return new MindPool(restored, max, false);
    }

    //  A burst 脑海 comes back empty, not full -- the cap survives, the contents do not. Used on respawn.
    public MindPool emptied() {return new MindPool(0L, max, false);}
}

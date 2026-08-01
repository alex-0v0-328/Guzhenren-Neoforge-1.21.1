package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One cell of the Mind Ocean: thoughts / wills / emotions [念/意/情]. The map key in MindData says which.
//  ⚠⚠ The RECORD cannot enforce the shape, because it does not know which cell it is -- WisdomType does
//  (`isBurstable`), so MindService clamps a hard-capped cell at the door. Here: current 0..max normal,
//  max..6/5×max buffer, past that it bursts; bufferUsed latches once past max.
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

    //  The most this cell may hold before it shatters: a fifth over the cap (his 1.0.0 spec; it was 2×
    //  until 2026-08-01). ⚠ Divides last, so a huge cap cannot overflow the multiply.
    public long burstAt() {return max / WisdomType.BURST_DENOMINATOR * WisdomType.BURST_NUMERATOR;}

    //  Shattered past the buffer. ⚠ Only a BURSTABLE cell ever gets here -- a hard-capped 意/情 is
    //  clamped at max by MindService, so it can never be lethal however it is written to.
    public boolean isOverflowing() {return current > burstAt();}

    public MindPool withCurrent(long v) {return new MindPool(v, max, bufferUsed);}
    public MindPool withMax(long v) {return new MindPool(current, v, bufferUsed);}

    //  Sleep restores toward the cap -- half the deficit if the buffer was used, else whole. Never reduces.
    public MindPool slept() {
        long restored = bufferUsed && current < max ? current + (max - current) / 2 : Math.max(current, max);
        return new MindPool(restored, max, false);
    }

    //  A burst cell comes back empty, not full -- the cap survives, the contents do not. Used on respawn.
    public MindPool emptied() {return new MindPool(0L, max, false);}
}

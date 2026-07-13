package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The "lifespan" (寿元) system. One in-game day: age +1, lifespan -1. See CLAUDE.md "Time, sleep,
//  death".
//
//  `lastDayIndex` is the overworld day we last billed the player for, and it is what makes aging
//  idempotent -- a relog, a dimension change or a double tick cannot charge twice, and ten days
//  offline are billed as exactly ten years. UNTRACKED means "never billed": the first tick adopts the
//  current day without aging.
public record LifespanData(long age, long lifespan, long lastDayIndex) {

    public static final long UNTRACKED = -1L;

    //  A new player is 14 with 86 years left: an ordinary mortal burns out at 100.
    public static final long DEFAULT_AGE      = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;

    public static final LifespanData DEFAULT = new LifespanData(DEFAULT_AGE, DEFAULT_LIFESPAN, UNTRACKED);

    public static final Codec<LifespanData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("age", DEFAULT_AGE).forGetter(LifespanData::age),
            Codec.LONG.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(LifespanData::lifespan),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(LifespanData::lastDayIndex)
    ).apply(instance, LifespanData::new));

    //  lastDayIndex is server bookkeeping, but it is two bytes on the wire and shipping the whole
    //  record keeps client and server holding the same value. Not worth a bespoke partial codec.
    public static final StreamCodec<ByteBuf, LifespanData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, LifespanData::age,
            ByteBufCodecs.VAR_LONG, LifespanData::lifespan,
            ByteBufCodecs.VAR_LONG, LifespanData::lastDayIndex,
            LifespanData::new);

    public LifespanData {
        age = Math.max(0L, age);
    }

    public boolean isExhausted() {return lifespan <= 0L;}
    public LifespanData withAge(long v) {return new LifespanData(v, lifespan, lastDayIndex);}
    public LifespanData withLifespan(long v) {return new LifespanData(age, v, lastDayIndex);}
    public LifespanData withLastDayIndex(long v) {return new LifespanData(age, lifespan, v);}
}

package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The "lifespan" (寿元) system. One in-game day ages the player by a year and burns a year of
//  remaining lifespan.
//
//  `lastDayIndex` is the overworld day index we last billed the player for. It makes aging
//  idempotent: a player who relogs, changes dimension, or is ticked twice in a day is not aged
//  twice, and a player who was offline for ten days is aged exactly ten years on login.
//  UNTRACKED means "never billed" -- the first tick adopts the current day without aging.
public record LifespanData(long age, long lifespan, long lastDayIndex) {

    public static final long UNTRACKED = -1L;
    public static final long DEFAULT_LIFESPAN = 100L;

    public static final LifespanData DEFAULT = new LifespanData(0L, DEFAULT_LIFESPAN, UNTRACKED);

    public static final Codec<LifespanData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("age", 0L).forGetter(LifespanData::age),
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

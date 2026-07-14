package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The body (肉身): 生死僵, 凡/仙, and 寿元. Soul and path are the body's too, but they carry a pool
//  and a map, so they keep their own attachments -- see CLAUDE.md "Architecture: player data".
//  lastDayIndex = last overworld day billed; makes aging idempotent and relog-safe.
public record BodyData(
        LifeState lifeState,
        LifeForm lifeForm,
        long age,
        long lifespan,
        long lastDayIndex
) {

    public static final long UNTRACKED = -1L;

    public static final long DEFAULT_AGE      = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;

    public static final BodyData DEFAULT = new BodyData(
            LifeState.ALIVE, LifeForm.MORTAL, DEFAULT_AGE, DEFAULT_LIFESPAN, UNTRACKED);

    public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LifeState.CODEC.optionalFieldOf("life_state", LifeState.ALIVE).forGetter(BodyData::lifeState),
            LifeForm.CODEC.optionalFieldOf("life_form", LifeForm.MORTAL).forGetter(BodyData::lifeForm),
            Codec.LONG.optionalFieldOf("age", DEFAULT_AGE).forGetter(BodyData::age),
            Codec.LONG.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(BodyData::lifespan),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(BodyData::lastDayIndex)
    ).apply(instance, BodyData::new));

    //  lastDayIndex is server bookkeeping, but two bytes -- not worth a partial codec.
    public static final StreamCodec<ByteBuf, BodyData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(LifeState.class), BodyData::lifeState,
            ModStreamCodecs.ofEnum(LifeForm.class), BodyData::lifeForm,
            ByteBufCodecs.VAR_LONG, BodyData::age,
            ByteBufCodecs.VAR_LONG, BodyData::lifespan,
            ByteBufCodecs.VAR_LONG, BodyData::lastDayIndex,
            BodyData::new);

    public BodyData {
        age = Math.max(0L, age);
    }

    public boolean isExhausted() {return lifespan <= 0L;}
    public boolean isAlive() {return lifeState == LifeState.ALIVE;}

    //  ---- withers ----
    public BodyData withLifeState(LifeState v) {return new BodyData(v, lifeForm, age, lifespan, lastDayIndex);}
    public BodyData withLifeForm(LifeForm v) {return new BodyData(lifeState, v, age, lifespan, lastDayIndex);}
    public BodyData withAge(long v) {return new BodyData(lifeState, lifeForm, v, lifespan, lastDayIndex);}
    public BodyData withLifespan(long v) {return new BodyData(lifeState, lifeForm, age, v, lastDayIndex);}
    public BodyData withLastDayIndex(long v) {return new BodyData(lifeState, lifeForm, age, lifespan, v);}

    //  One day billed: a year older, a year less to live. Age and lifespan move as one.
    public BodyData aged(long days, long today) {
        return new BodyData(lifeState, lifeForm, age + days, lifespan - days, today);
    }
}

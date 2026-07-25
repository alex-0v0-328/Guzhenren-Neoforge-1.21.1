package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The body [肉身]: life state, life form, lifespan and age.
//  Soul, path and qi are the body's too, but keep their own attachments.
//  lastDayIndex = last overworld day billed; makes aging idempotent and relog-safe.
public record BodyData(
        LifeState lifeState,
        LifeForm lifeForm,
        long age,
        long lifespan,
        long lastDayIndex,
        long deathQiLifespanLost
) {

    public static final long UNTRACKED = -1L;

    public static final long DEFAULT_AGE = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;

    public static final BodyData DEFAULT = new BodyData(
            LifeState.ALIVE, LifeForm.MORTAL, DEFAULT_AGE, DEFAULT_LIFESPAN, UNTRACKED, 0L);

    public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LifeState.CODEC.optionalFieldOf("life_state", LifeState.ALIVE).forGetter(BodyData::lifeState),
            LifeForm.CODEC.optionalFieldOf("life_form", LifeForm.MORTAL).forGetter(BodyData::lifeForm),
            Codec.LONG.optionalFieldOf("age", DEFAULT_AGE).forGetter(BodyData::age),
            Codec.LONG.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(BodyData::lifespan),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(BodyData::lastDayIndex),
            Codec.LONG.optionalFieldOf("death_qi_lifespan_lost", 0L).forGetter(BodyData::deathQiLifespanLost)
    ).apply(instance, BodyData::new));

    //  lastDayIndex is server bookkeeping, but two bytes -- not worth a partial codec. So is the debt.
    public static final StreamCodec<ByteBuf, BodyData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(LifeState.class), BodyData::lifeState,
            ModStreamCodecs.ofEnum(LifeForm.class), BodyData::lifeForm,
            ByteBufCodecs.VAR_LONG, BodyData::age,
            ByteBufCodecs.VAR_LONG, BodyData::lifespan,
            ByteBufCodecs.VAR_LONG, BodyData::lastDayIndex,
            ByteBufCodecs.VAR_LONG, BodyData::deathQiLifespanLost,
            BodyData::new);

    public BodyData {
        age = Math.max(0L, age);
        deathQiLifespanLost = Math.max(0L, deathQiLifespanLost);
    }

    public boolean isExhausted() {return lifespan <= 0L;}
    public boolean isAlive() {return lifeState == LifeState.ALIVE;}

    //  ---- withers ----
    //  ⚠ Six components run past 120 as one-liners, so these are blocks -- names stay spelled out.
    public BodyData withLifeState(LifeState v) {
        return new BodyData(v, lifeForm, age, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLifeForm(LifeForm v) {
        return new BodyData(lifeState, v, age, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withAge(long v) {
        return new BodyData(lifeState, lifeForm, v, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLifespan(long v) {
        return new BodyData(lifeState, lifeForm, age, v, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLastDayIndex(long v) {
        return new BodyData(lifeState, lifeForm, age, lifespan, v, deathQiLifespanLost);
    }

    //  How much lifespan Death Qi [死气] has taken so far. ⚠ Stored on the body, NOT on the effect:
    //  1.21.1's MobEffect has no expiry hook, and milk / effect clear / death would lose the tally.
    public BodyData withDeathQiLifespanLost(long v) {
        return new BodyData(lifeState, lifeForm, age, lifespan, lastDayIndex, v);
    }

    //  One day billed: a year older, a year less to live. Age and lifespan move as one.
    public BodyData aged(long days, long today) {
        return new BodyData(lifeState, lifeForm, age + days, lifespan - days, today, deathQiLifespanLost);
    }
}

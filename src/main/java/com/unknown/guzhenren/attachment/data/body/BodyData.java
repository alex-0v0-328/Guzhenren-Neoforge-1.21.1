package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

//  The body [肉身]: life state, life form, lifespan and age.
//  Soul, path and qi are the body's too, but keep their own attachments.
//  lastDayIndex = last overworld day billed; makes aging idempotent and relog-safe.
public record BodyData(
        LifeState lifeState,
        LifeForm lifeForm,
        Race race,
        long age,
        long lifespan,
        long lastDayIndex,
        long deathQiLifespanLost
) {

    public static final long UNTRACKED = -1L;

    public static final long DEFAULT_AGE = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;

    public static final BodyData DEFAULT = new BodyData(
            LifeState.ALIVE, LifeForm.MORTAL, Race.HUMAN, DEFAULT_AGE, DEFAULT_LIFESPAN, UNTRACKED, 0L);

    public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LifeState.CODEC.optionalFieldOf("life_state", LifeState.ALIVE).forGetter(BodyData::lifeState),
            LifeForm.CODEC.optionalFieldOf("life_form", LifeForm.MORTAL).forGetter(BodyData::lifeForm),
            Race.CODEC.optionalFieldOf("race", Race.HUMAN).forGetter(BodyData::race),
            Codec.LONG.optionalFieldOf("age", DEFAULT_AGE).forGetter(BodyData::age),
            Codec.LONG.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(BodyData::lifespan),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(BodyData::lastDayIndex),
            Codec.LONG.optionalFieldOf("death_qi_lifespan_lost", 0L).forGetter(BodyData::deathQiLifespanLost)
    ).apply(instance, BodyData::new));

    private static final StreamCodec<ByteBuf, LifeState> STATE = ModStreamCodecs.ofEnum(LifeState.class);
    private static final StreamCodec<ByteBuf, LifeForm> FORM = ModStreamCodecs.ofEnum(LifeForm.class);
    private static final StreamCodec<ByteBuf, Race> RACE = ModStreamCodecs.ofEnum(Race.class);

    //  lastDayIndex is server bookkeeping, but two bytes -- not worth a partial codec. So is the debt.
    //  ⚠⚠ Hand-written since race landed: SEVEN components, and StreamCodec.composite stops at six --
    //  the same wall Aperture hit. Encode and decode order must match BY HAND; nothing checks it.
    public static final StreamCodec<ByteBuf, BodyData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull BodyData decode(@NotNull ByteBuf buf) {
            return new BodyData(
                    STATE.decode(buf),
                    FORM.decode(buf),
                    RACE.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull BodyData value) {
            STATE.encode(buf, value.lifeState());
            FORM.encode(buf, value.lifeForm());
            RACE.encode(buf, value.race());
            ByteBufCodecs.VAR_LONG.encode(buf, value.age());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lifespan());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lastDayIndex());
            ByteBufCodecs.VAR_LONG.encode(buf, value.deathQiLifespanLost());
        }
    };

    public BodyData {
        age = Math.max(0L, age);
        deathQiLifespanLost = Math.max(0L, deathQiLifespanLost);
    }

    public boolean isExhausted() {return lifespan <= 0L;}
    public boolean isAlive() {return lifeState == LifeState.ALIVE;}

    //  ---- withers ----
    //  ⚠ Seven components run past 120 as one-liners, so these are blocks -- names stay spelled out.
    public BodyData withLifeState(LifeState v) {
        return new BodyData(v, lifeForm, race, age, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLifeForm(LifeForm v) {
        return new BodyData(lifeState, v, race, age, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withRace(Race v) {
        return new BodyData(lifeState, lifeForm, v, age, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withAge(long v) {
        return new BodyData(lifeState, lifeForm, race, v, lifespan, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLifespan(long v) {
        return new BodyData(lifeState, lifeForm, race, age, v, lastDayIndex, deathQiLifespanLost);
    }
    public BodyData withLastDayIndex(long v) {
        return new BodyData(lifeState, lifeForm, race, age, lifespan, v, deathQiLifespanLost);
    }

    //  How much lifespan Death Qi [死气] has taken so far. ⚠ Stored on the body, NOT on the effect:
    //  1.21.1's MobEffect has no expiry hook, and milk / effect clear / death would lose the tally.
    public BodyData withDeathQiLifespanLost(long v) {
        return new BodyData(lifeState, lifeForm, race, age, lifespan, lastDayIndex, v);
    }

    //  One day billed: a year older, a year less to live. Age and lifespan move as one.
    public BodyData aged(long days, long today) {
        return new BodyData(
                lifeState, lifeForm, race, age + days, lifespan - days, today, deathQiLifespanLost);
    }
}

package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Body [肉身] state: what the player currently is, as opposed to what they can do.
 *
 * <p>⚠ {@code lifeForm} is the ONE life-state value. It replaced two scattered ones, and nothing may go
 * back to inferring alive-or-dead by reading several fields and reasoning about them.
 *
 * @author Alex
 * @since 1.0.0
 */
public record BodyData(
        LifeForm lifeForm,
        Race race,
        long age,
        long lifespan,
        long lastDayIndex,
        long deathQiLifespanLost,
        long halfZombieEndTick,
        int zombieTier
) {

    public static final long UNTRACKED = -1L;
    public static final int NO_ZOMBIE_TIER = -1;

    public static final long DEFAULT_AGE = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;
    public static final long ZOMBIE_LIFESPAN = 1L;

    public static final long RELAPSE_WINDOW_TICKS = 5L * Ticks.MINUTE;

    public static final BodyData DEFAULT = new BodyData(
            LifeForm.ALIVE, Race.HUMAN, DEFAULT_AGE, DEFAULT_LIFESPAN, UNTRACKED, 0L, UNTRACKED, NO_ZOMBIE_TIER);

    public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LifeForm.CODEC.optionalFieldOf("life_form", LifeForm.ALIVE).forGetter(BodyData::lifeForm),
            Race.CODEC.optionalFieldOf("race", Race.HUMAN).forGetter(BodyData::race),
            Codec.LONG.optionalFieldOf("age", DEFAULT_AGE).forGetter(BodyData::age),
            Codec.LONG.optionalFieldOf("lifespan", DEFAULT_LIFESPAN).forGetter(BodyData::lifespan),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(BodyData::lastDayIndex),
            Codec.LONG.optionalFieldOf("death_qi_lifespan_lost", 0L).forGetter(BodyData::deathQiLifespanLost),
            Codec.LONG.optionalFieldOf("half_zombie_end_tick", UNTRACKED).forGetter(BodyData::halfZombieEndTick),
            Codec.INT.optionalFieldOf("zombie_tier", NO_ZOMBIE_TIER).forGetter(BodyData::zombieTier)
    ).apply(instance, BodyData::new));

    private static final StreamCodec<ByteBuf, LifeForm> FORM = ModStreamCodecs.ofEnum(LifeForm.class);
    private static final StreamCodec<ByteBuf, Race> RACE = ModStreamCodecs.ofEnum(Race.class);

    public static final StreamCodec<ByteBuf, BodyData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull BodyData decode(@NotNull ByteBuf buf) {
            return new BodyData(
                    FORM.decode(buf),
                    RACE.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull BodyData value) {
            FORM.encode(buf, value.lifeForm());
            RACE.encode(buf, value.race());
            ByteBufCodecs.VAR_LONG.encode(buf, value.age());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lifespan());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lastDayIndex());
            ByteBufCodecs.VAR_LONG.encode(buf, value.deathQiLifespanLost());
            ByteBufCodecs.VAR_LONG.encode(buf, value.halfZombieEndTick());
            ByteBufCodecs.VAR_INT.encode(buf, value.zombieTier());
        }
    };

    public BodyData {
        age = Math.max(0L, age);
        deathQiLifespanLost = Math.max(0L, deathQiLifespanLost);
    }

    public boolean isExhausted() {return lifespan <= 0L;}
    public boolean isZombie() {return lifeForm.isZombie();}
    public boolean isHalfZombie() {return lifeForm.isHalfZombie();}

    public boolean halfZombieRanOut(long now) {return halfZombieEndTick != UNTRACKED && now >= halfZombieEndTick;}
    public long halfZombieTicksLeft(long now) {return Math.max(0L, halfZombieEndTick - now);}

    public boolean withinRelapseWindow(long now) {
        return halfZombieEndTick != UNTRACKED && now < halfZombieEndTick + RELAPSE_WINDOW_TICKS;
    }

    public BodyData withLifeForm(LifeForm v) {
        return new BodyData(v, race, age, lifespan, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withRace(Race v) {
        return new BodyData(lifeForm, v, age, lifespan, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withAge(long v) {
        return new BodyData(lifeForm, race, v, lifespan, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withLifespan(long v) {
        return new BodyData(lifeForm, race, age, v, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withLastDayIndex(long v) {
        return new BodyData(lifeForm, race, age, lifespan, v, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withDeathQiLifespanLost(long v) {
        return new BodyData(lifeForm, race, age, lifespan, lastDayIndex, v,
                halfZombieEndTick, zombieTier);
    }
    public BodyData withHalfZombieEndTick(long v) {
        return new BodyData(lifeForm, race, age, lifespan, lastDayIndex, deathQiLifespanLost,
                v, zombieTier);
    }
    public BodyData withZombieTier(int v) {
        return new BodyData(lifeForm, race, age, lifespan, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, v);
    }

    public BodyData aged(long days, long today) {
        return new BodyData(lifeForm, race, age + days, lifespan - days, today, deathQiLifespanLost,
                halfZombieEndTick, zombieTier);
    }
}

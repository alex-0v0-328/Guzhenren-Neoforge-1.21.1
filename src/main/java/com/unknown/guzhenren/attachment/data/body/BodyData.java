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
 * <p>Immutable record attachment keyed {@code body_data}; {@link
 * com.unknown.guzhenren.attachment.service.body.BodyService} is the only writer. Nine components:
 * {@code lifeForm}, {@code race}, age/lifespan in parts (not years), two day-clock anchors, the
 * death-qi debt tally, the half-zombie end tick, the zombie tier, and the lifespan billing anchor.
 *
 * <p>⚠ {@code lifeForm} is the ONE life-state value -- it replaced two scattered ones, and nothing may
 * go back to inferring alive-or-dead by reading several fields. ⚠ This record has NINE components, so
 * its {@code STREAM_CODEC} is handwritten -- {@code StreamCodec.composite} stops at six, and the
 * encode/decode order matches by hand with no compile-time check. ⚠ The time anchors default to
 * {@code UNTRACKED} (-1), never {@code 0}: zero is a real game time, so a fresh world would read as
 * "already started". ⚠ {@code lived()} moves age and lifespan together in one call -- they are never
 * written apart, one is what he has spent and the other what is left.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.BodyService
 * @see com.unknown.guzhenren.custom.enums.body.LifeForm
 * @since 1.0.0
 */

public record BodyData(
        LifeForm lifeForm,
        Race race,
        long ageParts,
        long lifespanParts,
        long lastDayIndex,
        long deathQiLifespanLost,
        long halfZombieEndTick,
        int zombieTier,
        long lastBilledTick
) {

    public static final long UNTRACKED = -1L;
    public static final int NO_ZOMBIE_TIER = -1;

    /**
     * ⚠ A year is a game day. The unit is finer than a tick so that a Time Path form which one day SLOWS
     * a life, rather than hastening one, still divides a heartbeat's share evenly.
     */
    public static final long PARTS_PER_TICK = 6L;
    public static final long PARTS_PER_YEAR = Ticks.DAY * PARTS_PER_TICK;

    public static final long DEFAULT_AGE = 14L;
    public static final long DEFAULT_LIFESPAN = 86L;
    public static final long ZOMBIE_LIFESPAN = 1L;

    public static final long RELAPSE_WINDOW_TICKS = 5L * Ticks.MINUTE;

    public static final BodyData DEFAULT = new BodyData(LifeForm.ALIVE, Race.HUMAN,
            parts(DEFAULT_AGE), parts(DEFAULT_LIFESPAN), UNTRACKED, 0L, UNTRACKED, NO_ZOMBIE_TIER, UNTRACKED);

    public static long parts(long years) {return years * PARTS_PER_YEAR;}

    public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LifeForm.CODEC.optionalFieldOf("life_form", LifeForm.ALIVE).forGetter(BodyData::lifeForm),
            Race.CODEC.optionalFieldOf("race", Race.HUMAN).forGetter(BodyData::race),
            Codec.LONG.optionalFieldOf("age_parts", parts(DEFAULT_AGE)).forGetter(BodyData::ageParts),
            Codec.LONG.optionalFieldOf("lifespan_parts", parts(DEFAULT_LIFESPAN)).forGetter(BodyData::lifespanParts),
            Codec.LONG.optionalFieldOf("last_day_index", UNTRACKED).forGetter(BodyData::lastDayIndex),
            Codec.LONG.optionalFieldOf("death_qi_lifespan_lost", 0L).forGetter(BodyData::deathQiLifespanLost),
            Codec.LONG.optionalFieldOf("half_zombie_end_tick", UNTRACKED).forGetter(BodyData::halfZombieEndTick),
            Codec.INT.optionalFieldOf("zombie_tier", NO_ZOMBIE_TIER).forGetter(BodyData::zombieTier),
            Codec.LONG.optionalFieldOf("last_billed_tick", UNTRACKED).forGetter(BodyData::lastBilledTick)
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
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull BodyData value) {
            FORM.encode(buf, value.lifeForm());
            RACE.encode(buf, value.race());
            ByteBufCodecs.VAR_LONG.encode(buf, value.ageParts());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lifespanParts());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lastDayIndex());
            ByteBufCodecs.VAR_LONG.encode(buf, value.deathQiLifespanLost());
            ByteBufCodecs.VAR_LONG.encode(buf, value.halfZombieEndTick());
            ByteBufCodecs.VAR_INT.encode(buf, value.zombieTier());
            ByteBufCodecs.VAR_LONG.encode(buf, value.lastBilledTick());
        }
    };

    public BodyData {
        ageParts = Math.max(0L, ageParts);
        deathQiLifespanLost = Math.max(0L, deathQiLifespanLost);
        if (!lifeForm.isAnyZombie()) zombieTier = NO_ZOMBIE_TIER;
    }

    public boolean isExhausted() {return lifespanParts <= 0L;}
    public boolean isZombie() {return lifeForm.isZombie();}
    public boolean isHalfZombie() {return lifeForm.isHalfZombie();}

    public double ageYears() {return ageParts / (double) PARTS_PER_YEAR;}
    public double lifespanYears() {return lifespanParts / (double) PARTS_PER_YEAR;}

    public boolean halfZombieRanOut(long now) {return halfZombieEndTick != UNTRACKED && now >= halfZombieEndTick;}
    public long halfZombieTicksLeft(long now) {return Math.max(0L, halfZombieEndTick - now);}

    public boolean withinRelapseWindow(long now) {
        return halfZombieEndTick != UNTRACKED && now < halfZombieEndTick + RELAPSE_WINDOW_TICKS;
    }

    public BodyData withLifeForm(LifeForm v) {
        return new BodyData(v, race, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, v.isAnyZombie() ? zombieTier : NO_ZOMBIE_TIER, lastBilledTick);
    }

    public BodyData revived() {
        return new BodyData(LifeForm.ALIVE, race, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                UNTRACKED, NO_ZOMBIE_TIER, lastBilledTick);
    }
    public BodyData withRace(Race v) {
        return new BodyData(lifeForm, v, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier, lastBilledTick);
    }
    public BodyData withAgeParts(long v) {
        return new BodyData(lifeForm, race, v, lifespanParts, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier, lastBilledTick);
    }
    public BodyData withLifespanParts(long v) {
        return new BodyData(lifeForm, race, ageParts, v, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier, lastBilledTick);
    }
    public BodyData withLastDayIndex(long v) {
        return new BodyData(lifeForm, race, ageParts, lifespanParts, v, deathQiLifespanLost,
                halfZombieEndTick, zombieTier, lastBilledTick);
    }
    public BodyData withDeathQiLifespanLost(long v) {
        return new BodyData(lifeForm, race, ageParts, lifespanParts, lastDayIndex, v,
                halfZombieEndTick, zombieTier, lastBilledTick);
    }
    public BodyData withHalfZombieEndTick(long v) {
        return new BodyData(lifeForm, race, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                v, zombieTier, lastBilledTick);
    }
    public BodyData withZombieTier(int v) {
        return new BodyData(lifeForm, race, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, v, lastBilledTick);
    }
    public BodyData withLastBilledTick(long v) {
        return new BodyData(lifeForm, race, ageParts, lifespanParts, lastDayIndex, deathQiLifespanLost,
                halfZombieEndTick, zombieTier, v);
    }

    /**
     * ⚠ The two move together and are never written apart: one is what he has spent, the other what is left.
     */
    public BodyData lived(long parts, long billedTick) {
        return new BodyData(lifeForm, race, ageParts + parts, lifespanParts - parts, lastDayIndex,
                deathQiLifespanLost, halfZombieEndTick, zombieTier, billedTick);
    }
}

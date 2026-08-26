package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.Ticks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Nourishing the Aperture [温养空窍]: whether the cultivation is running, and how far it has come.
 *
 * <p>Immutable record attachment keyed {@code nourish_data}; {@link
 * com.unknown.guzhenren.attachment.service.aperture.NourishService} is the only writer. Four
 * components: a running flag, a 0..100 progress percent, a starvation-since tick, and the Stone
 * Aperture Gu's [石窍蛊] permanent lock. Progress clamps to {@code [0, FULL]} in the compact ctor;
 * the other three are stored verbatim.
 *
 * <p>⚠ {@code starvedSinceTick} defaults to {@code NOT_STARVED} (-1), never {@code 0} -- zero is a real
 * game time, so a fresh world would read as having been starved since the beginning of it. Same trap
 * as {@code halfZombieEndTick} and {@code USED_AT} on the Gu clock. ⚠ {@code isFull()} is the gate
 * that lets {@link
 * com.unknown.guzhenren.attachment.service.aperture.NourishService#impactWall} fire, and the strike
 * zeroes progress whether it succeeds or fails -- that IS how "you must run a whole peak round
 * again" is implemented, with no separate flag. ⚠ {@code petrified} stops the essence regen and
 * BOTH cultivation buttons; {@link
 * com.unknown.guzhenren.attachment.service.aperture.NourishService#petrify} sets it, and only the
 * full-pressure conversion or {@code resetAll} clears it.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.aperture.NourishService
 * @since 1.0.0
 */

public record NourishData(boolean cultivating, int progress, long starvedSinceTick, boolean petrified) {

    public static final int FULL = 100;
    public static final long NOT_STARVED = -1L;
    public static final long STARVE_GRACE_TICKS = 10L * Ticks.SECOND;

    public static final NourishData DEFAULT = new NourishData(false, 0, NOT_STARVED, false);
    public static final NourishData PETRIFIED = new NourishData(false, 0, NOT_STARVED, true);

    public static final Codec<NourishData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("cultivating", false).forGetter(NourishData::cultivating),
            Codec.INT.optionalFieldOf("progress", 0).forGetter(NourishData::progress),
            Codec.LONG.optionalFieldOf("starved_since_tick", NOT_STARVED).forGetter(NourishData::starvedSinceTick),
            Codec.BOOL.optionalFieldOf("petrified", false).forGetter(NourishData::petrified)
    ).apply(instance, NourishData::new));

    public static final StreamCodec<ByteBuf, NourishData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, NourishData::cultivating,
            ByteBufCodecs.VAR_INT, NourishData::progress,
            ByteBufCodecs.VAR_LONG, NourishData::starvedSinceTick,
            ByteBufCodecs.BOOL, NourishData::petrified,
            NourishData::new);

    public NourishData {
        progress = Math.clamp(progress, 0, FULL);
    }

    public boolean isFull() {return progress >= FULL;}
    public boolean isStarving() {return starvedSinceTick != NOT_STARVED;}
    public float fraction() {return progress / (float) FULL;}
    public boolean starvedOut(long now) {return isStarving() && now - starvedSinceTick >= STARVE_GRACE_TICKS;}

    public NourishData withCultivating(boolean v) {return new NourishData(v, progress, starvedSinceTick, petrified);}
    public NourishData withProgress(int v) {return new NourishData(cultivating, v, starvedSinceTick, petrified);}
    public NourishData withStarvedSince(long v) {return new NourishData(cultivating, progress, v, petrified);}
}

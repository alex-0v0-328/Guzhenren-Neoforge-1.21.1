package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.Ticks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Nourishing the Aperture [温养空窍]: whether a run is going, and which aperture it feeds.
 *
 * <p>Immutable record attachment keyed {@code nourish_data}; {@link
 * com.unknown.guzhenren.attachment.service.aperture.NourishService} is the only writer. Three
 * components: a running flag, the target aperture index, and a starvation-since tick. The progress
 * percent and the petrified latch live on {@link Aperture} itself, one set per aperture.
 *
 * <p>⚠ {@code starvedSinceTick} defaults to {@code NOT_STARVED} (-1), never {@code 0} -- zero is a real
 * game time, so a fresh world would read as having been starved since the beginning of it. Same trap
 * as {@code halfZombieEndTick} and {@code USED_AT} on the Gu clock. ⚠ {@code target} is only trusted
 * after the service has clamped it against the apertures that exist, never straight from the wire.
 *
 * @author Alex
 * @version 1.0.0
 * @see Aperture
 * @see com.unknown.guzhenren.attachment.service.aperture.NourishService
 * @since 1.0.0
 */

public record NourishData(boolean cultivating, int target, long starvedSinceTick) {

    public static final int FULL = 100;
    public static final long NOT_STARVED = -1L;
    public static final long STARVE_GRACE_TICKS = 10L * Ticks.SECOND;

    public static final NourishData DEFAULT = new NourishData(false, ApertureData.PRIMARY, NOT_STARVED);

    public static final Codec<NourishData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("cultivating", false).forGetter(NourishData::cultivating),
            Codec.INT.optionalFieldOf("target", ApertureData.PRIMARY).forGetter(NourishData::target),
            Codec.LONG.optionalFieldOf("starved_since_tick", NOT_STARVED).forGetter(NourishData::starvedSinceTick)
    ).apply(instance, NourishData::new));

    public static final StreamCodec<ByteBuf, NourishData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, NourishData::cultivating,
            ByteBufCodecs.VAR_INT, NourishData::target,
            ByteBufCodecs.VAR_LONG, NourishData::starvedSinceTick,
            NourishData::new);

    public boolean isStarving() {return starvedSinceTick != NOT_STARVED;}
    public boolean starvedOut(long now) {return isStarving() && now - starvedSinceTick >= STARVE_GRACE_TICKS;}

    public NourishData withCultivating(boolean v) {return new NourishData(v, target, starvedSinceTick);}
    public NourishData withTarget(int v) {return new NourishData(cultivating, v, starvedSinceTick);}
    public NourishData withStarvedSince(long v) {return new NourishData(cultivating, target, v);}
}

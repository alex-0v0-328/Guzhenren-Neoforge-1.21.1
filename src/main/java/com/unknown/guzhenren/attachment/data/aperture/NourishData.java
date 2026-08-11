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
 * <p>⚠ The starved anchor defaults to a sentinel, never to zero -- zero is a real game time, so a
 * fresh world would read as having been starved since the beginning of it.
 *
 * @author Alex
 * @since 1.0.0
 */
public record NourishData(boolean cultivating, int progress, long starvedSinceTick) {

    public static final int FULL = 100;
    public static final long NOT_STARVED = -1L;
    public static final long STARVE_GRACE_TICKS = 10L * Ticks.SECOND;

    public static final NourishData DEFAULT = new NourishData(false, 0, NOT_STARVED);

    public static final Codec<NourishData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("cultivating", false).forGetter(NourishData::cultivating),
            Codec.INT.optionalFieldOf("progress", 0).forGetter(NourishData::progress),
            Codec.LONG.optionalFieldOf("starved_since_tick", NOT_STARVED).forGetter(NourishData::starvedSinceTick)
    ).apply(instance, NourishData::new));

    public static final StreamCodec<ByteBuf, NourishData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, NourishData::cultivating,
            ByteBufCodecs.VAR_INT, NourishData::progress,
            ByteBufCodecs.VAR_LONG, NourishData::starvedSinceTick,
            NourishData::new);

    public NourishData {
        progress = Math.clamp(progress, 0, FULL);
    }

    public boolean isFull() {return progress >= FULL;}
    public boolean isStarving() {return starvedSinceTick != NOT_STARVED;}
    public float fraction() {return progress / (float) FULL;}
    public boolean starvedOut(long now) {return isStarving() && now - starvedSinceTick >= STARVE_GRACE_TICKS;}

    public NourishData withCultivating(boolean v) {return new NourishData(v, progress, starvedSinceTick);}
    public NourishData withProgress(int v) {return new NourishData(cultivating, v, starvedSinceTick);}
    public NourishData withStarvedSince(long v) {return new NourishData(cultivating, progress, v);}
}

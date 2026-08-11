package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.soul.SoulTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Soul [魂魄], the one pool that is lethal at the bottom.
 *
 * <p>⚠ Its cap is stored rather than derived, because nothing else determines it. Compare
 * {@link StaminaData}, whose cap has two sources and therefore stores only the earned part.
 *
 * @author Alex
 * @since 1.0.0
 */
public record SoulData(long maxSoul, long currentSoul) {

    public static final long DEFAULT_MAX_SOUL = 100L;

    public static final long REVIVED_SOUL = 1L;

    public static final SoulData DEFAULT = new SoulData(DEFAULT_MAX_SOUL, DEFAULT_MAX_SOUL);

    public static final Codec<SoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("max_soul", DEFAULT_MAX_SOUL).forGetter(SoulData::maxSoul),
            Codec.LONG.optionalFieldOf("current_soul", DEFAULT_MAX_SOUL).forGetter(SoulData::currentSoul)
    ).apply(instance, SoulData::new));

    public static final StreamCodec<ByteBuf, SoulData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SoulData::maxSoul,
            ByteBufCodecs.VAR_LONG, SoulData::currentSoul,
            SoulData::new);

    public SoulData {
        maxSoul = Math.max(0L, maxSoul);
        currentSoul = Math.clamp(currentSoul, 0L, maxSoul);
    }

    public SoulTier tier() {return SoulTier.fromSoul(maxSoul);}

    public boolean isCollapsed() {return currentSoul <= 0L;}
    public SoulData withMaxSoul(long v) {return new SoulData(v, currentSoul);}
    public SoulData withCurrentSoul(long v) {return new SoulData(maxSoul, v);}
    public SoulData refilled() {return new SoulData(maxSoul, maxSoul);}

    public SoulData revived() {
        return new SoulData(maxSoul > 0L ? maxSoul : DEFAULT_MAX_SOUL, REVIVED_SOUL);
    }
}

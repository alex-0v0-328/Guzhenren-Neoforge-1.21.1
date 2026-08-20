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
 * <p>Immutable record attachment keyed {@code soul_data}; {@link SoulService} is the only writer. The
 * compact constructor floors {@code maxSoul} at zero and clamps {@code currentSoul} to {@code [0, max]},
 * so a cap of 0 also lands current at 0 -- one check catches both.
 *
 * <p>⚠ Its cap is STORED rather than derived, because nothing else determines it -- compare
 * Epic Fight stamina, whose current value and cap are owned by the combat system. ⚠
 * {@code revived()} returns currentSoul {@code 1} (not 0) and restores {@code DEFAULT_MAX_SOUL} when
 * the cap itself was 0: a respawn may never hand back a value the lethal check would fire on. ⚠ The
 * {@code SoulTier} is derived from {@code maxSoul} via {@code SoulTier.fromSoul}, never stored.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see SoulService
 * @see com.unknown.guzhenren.custom.enums.soul.SoulTier
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

package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.soul.SoulTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The soul (魂魄) system. Tier (一人魂 / 十人魂 / ...) derived from maxSoul, never stored.
public record SoulData(long maxSoul, long currentSoul) {

    public static final long DEFAULT_MAX_SOUL = 100L;

    //  What a respawn hands back after a collapse -- see revived(). Never 0: 0 is the death check.
    public static final long REVIVED_SOUL = 1L;

    public static final SoulData DEFAULT = new SoulData(DEFAULT_MAX_SOUL, DEFAULT_MAX_SOUL);

    //  NBT keys spell the system out -- a bare "max" would be a coin flip.
    public static final Codec<SoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("max_soul", DEFAULT_MAX_SOUL).forGetter(SoulData::maxSoul),
            Codec.LONG.optionalFieldOf("current_soul", DEFAULT_MAX_SOUL).forGetter(SoulData::currentSoul)
    ).apply(instance, SoulData::new));

    public static final StreamCodec<ByteBuf, SoulData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SoulData::maxSoul,
            ByteBufCodecs.VAR_LONG, SoulData::currentSoul,
            SoulData::new);

    //  current may hit 0 -- soul collapse, PlayerTickEvents kills for it. It may not exceed max.
    public SoulData {
        maxSoul = Math.max(0L, maxSoul);
        currentSoul = Math.clamp(currentSoul, 0L, maxSoul);
    }

    public SoulTier tier() {return SoulTier.fromSoul(maxSoul);}

    //  A cap of 0 clamps current to 0, so one check covers a drained pool and a destroyed one alike.
    public boolean isCollapsed() {return currentSoul <= 0L;}
    public SoulData withMaxSoul(long v) {return new SoulData(v, currentSoul);}
    public SoulData withCurrentSoul(long v) {return new SoulData(maxSoul, v);}
    public SoulData refilled() {return new SoulData(maxSoul, maxSoul);}

    //  Respawn: 0 is death, so it cannot be what a respawn hands back -- one spark instead. A cap of 0
    //  is not survivable either, so it resets, exactly as an exhausted lifespan does.
    public SoulData revived() {
        return new SoulData(maxSoul > 0L ? maxSoul : DEFAULT_MAX_SOUL, REVIVED_SOUL);
    }
}

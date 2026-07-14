package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.core.GuSoulTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The soul (魂魄) system. 100 = one person's worth, hence the default.
//  The title (一人魂 / 十人魂 / ...) is derived from maxSoul, never stored.
public record SoulData(long maxSoul, long currentSoul) {

    public static final long DEFAULT_MAX_SOUL = 100L;

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

    public GuSoulTier tier() {return GuSoulTier.fromSoul(maxSoul);}
    public boolean isCollapsed() {return currentSoul <= 0L;}
    public SoulData withMaxSoul(long v) {return new SoulData(v, currentSoul);}
    public SoulData withCurrentSoul(long v) {return new SoulData(maxSoul, v);}
    public SoulData refilled() {return new SoulData(maxSoul, maxSoul);}
}

package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.wisdom.GuBrilliance;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

//  The "mind" (智道) system: 才情 and the 脑海's three cells. See CLAUDE.md "Wisdom".
//  Dense, unlike PathData -- a missing key is filled from the enum's default capacity.
public record MindData(GuBrilliance brilliance, Map<GuWisdomType, MindPool> pools) {

    public static final MindData DEFAULT = new MindData(GuBrilliance.ORDINARY, Map.of());

    public static final Codec<MindData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuBrilliance.CODEC.optionalFieldOf("brilliance", GuBrilliance.ORDINARY).forGetter(MindData::brilliance),
            Codec.unboundedMap(GuWisdomType.CODEC, MindPool.CODEC)
                    .optionalFieldOf("pools", Map.of()).forGetter(MindData::pools)
    ).apply(instance, MindData::new));

    public static final StreamCodec<ByteBuf, MindData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuBrilliance.class), MindData::brilliance,
            ModStreamCodecs.enumMap(GuWisdomType.class, MindPool.STREAM_CODEC), MindData::pools,
            MindData::new);

    public MindData {
        //  EnumMap: stable ordinal order on the wire and in NBT.
        Map<GuWisdomType, MindPool> dense = new EnumMap<>(GuWisdomType.class);
        for (GuWisdomType type : GuWisdomType.values()) {
            dense.put(type, pools.getOrDefault(type, MindPool.of(type)));
        }
        pools = Collections.unmodifiableMap(dense);
    }

    //  A newborn: empty cells at their default caps, and a rolled 才情. See CLAUDE.md "Birth".
    public static MindData newborn() {return new MindData(GuBrilliance.randomBrilliance(), Map.of());}

    public MindPool pool(GuWisdomType type) {return pools.get(type);}
    public boolean isOverflowing() {return pools.values().stream().anyMatch(MindPool::isOverflowing);}
    public MindData withBrilliance(GuBrilliance v) {return new MindData(v, pools);}

    public MindData with(GuWisdomType type, MindPool pool) {
        Map<GuWisdomType, MindPool> next = new EnumMap<>(GuWisdomType.class);
        next.putAll(pools);
        next.put(type, pool);
        return new MindData(brilliance, next);
    }

    //  A burst 脑海 comes back empty -- every cell, not just the one that burst. Used on respawn.
    public MindData emptied() {
        Map<GuWisdomType, MindPool> next = new EnumMap<>(GuWisdomType.class);
        pools.forEach((type, pool) -> next.put(type, pool.emptied()));
        return new MindData(brilliance, next);
    }
}

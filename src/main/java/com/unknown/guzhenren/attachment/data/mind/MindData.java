package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

public record MindData(Brilliance brilliance, Map<WisdomType, MindPool> pools) {

    public static final MindData DEFAULT = new MindData(Brilliance.ORDINARY, Map.of());

    public static final Codec<MindData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Brilliance.CODEC.optionalFieldOf("brilliance", Brilliance.ORDINARY).forGetter(MindData::brilliance),
            Codec.unboundedMap(WisdomType.CODEC, MindPool.CODEC)
                    .optionalFieldOf("pools", Map.of()).forGetter(MindData::pools)
    ).apply(instance, MindData::new));

    public static final StreamCodec<ByteBuf, MindData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(Brilliance.class), MindData::brilliance,
            ModStreamCodecs.enumMap(WisdomType.class, MindPool.STREAM_CODEC), MindData::pools,
            MindData::new);

    public MindData {
        Map<WisdomType, MindPool> dense = new EnumMap<>(WisdomType.class);
        for (WisdomType type : WisdomType.values()) {
            dense.put(type, pools.getOrDefault(type, MindPool.of(type)));
        }
        pools = Collections.unmodifiableMap(dense);
    }

    public static MindData newborn() {return new MindData(Brilliance.randomBrilliance(), Map.of());}

    public MindPool pool(WisdomType type) {return pools.get(type);}
    public boolean isOverflowing() {return pools.values().stream().anyMatch(MindPool::isOverflowing);}
    public MindData withBrilliance(Brilliance v) {return new MindData(v, pools);}

    public MindData with(WisdomType type, MindPool pool) {
        Map<WisdomType, MindPool> next = new EnumMap<>(WisdomType.class);
        next.putAll(pools);
        next.put(type, pool);
        return new MindData(brilliance, next);
    }

    public MindData emptied() {
        Map<WisdomType, MindPool> next = new EnumMap<>(WisdomType.class);
        pools.forEach((type, pool) -> next.put(type, pool.emptied()));
        return new MindData(brilliance, next);
    }
}

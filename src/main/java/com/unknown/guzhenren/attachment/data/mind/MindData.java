package com.unknown.guzhenren.attachment.data.mind;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

//  The "mind" (智道) system: the 脑海 and its three cells. See CLAUDE.md "Wisdom".
//  Dense, unlike PathData -- a missing key is filled from the enum's default capacity.
public record MindData(Map<GuWisdomType, MindPool> pools) {

    public static final MindData DEFAULT = new MindData(Map.of());

    public static final Codec<MindData> CODEC = Codec.unboundedMap(GuWisdomType.CODEC, MindPool.CODEC)
            .xmap(MindData::new, MindData::pools);

    public static final StreamCodec<ByteBuf, MindData> STREAM_CODEC =
            ModStreamCodecs.enumMap(GuWisdomType.class, MindPool.STREAM_CODEC).map(MindData::new, MindData::pools);

    public MindData {
        //  EnumMap: stable ordinal order on the wire and in NBT.
        Map<GuWisdomType, MindPool> dense = new EnumMap<>(GuWisdomType.class);
        for (GuWisdomType type : GuWisdomType.values()) {
            dense.put(type, pools.getOrDefault(type, MindPool.of(type)));
        }
        pools = Collections.unmodifiableMap(dense);
    }

    public MindPool pool(GuWisdomType type) {return pools.get(type);}
    public boolean isOverflowing() {return pools.values().stream().anyMatch(MindPool::isOverflowing);}

    public MindData with(GuWisdomType type, MindPool pool) {
        Map<GuWisdomType, MindPool> next = new EnumMap<>(GuWisdomType.class);
        next.putAll(pools);
        next.put(type, pool);
        return new MindData(next);
    }

    //  Overflow is lethal, so the only way back is down to the cap. Used on respawn.
    public MindData clamped() {
        Map<GuWisdomType, MindPool> next = new EnumMap<>(GuWisdomType.class);
        pools.forEach((type, pool) -> next.put(type, pool.clamped()));
        return new MindData(next);
    }
}

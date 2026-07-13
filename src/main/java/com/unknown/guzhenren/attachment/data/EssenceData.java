package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The "essence" (真元) system. Only the current pool is stored -- the cap is a pure function of
//  CoreData, see EssenceService.maxEssence.
//
//  The sub-integer carry of natural regeneration deliberately does NOT live here; it is a separate,
//  unsynced attachment (ModAttachments.ESSENCE_CARRY) so that a regen step which does not move the
//  pool does not push a packet.
public record EssenceData(long current) {

    public static final EssenceData DEFAULT = new EssenceData(0L);

    public static final Codec<EssenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("current", 0L).forGetter(EssenceData::current)
    ).apply(instance, EssenceData::new));

    public static final StreamCodec<ByteBuf, EssenceData> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(EssenceData::new, EssenceData::current);

    public EssenceData {
        current = Math.max(0L, current);
    }

    public EssenceData withCurrent(long v) {return new EssenceData(v);}
}

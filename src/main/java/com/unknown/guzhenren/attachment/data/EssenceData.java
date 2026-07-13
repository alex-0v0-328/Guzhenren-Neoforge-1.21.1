package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The "essence" (真元) system. Only the current pool is stored -- the cap is a pure function of
//  CoreData (EssenceService.maxEssence), and the sub-integer regen carry is a separate unsynced
//  attachment (ModAttachments.ESSENCE_CARRY). Neither belongs here; see CLAUDE.md "Networking".
public record EssenceData(long currentEssence) {

    public static final EssenceData DEFAULT = new EssenceData(0L);

    public static final Codec<EssenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("current_essence", 0L).forGetter(EssenceData::currentEssence)
    ).apply(instance, EssenceData::new));

    public static final StreamCodec<ByteBuf, EssenceData> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(EssenceData::new, EssenceData::currentEssence);

    public EssenceData {
        currentEssence = Math.max(0L, currentEssence);
    }

    public EssenceData withCurrentEssence(long v) {return new EssenceData(v);}
}

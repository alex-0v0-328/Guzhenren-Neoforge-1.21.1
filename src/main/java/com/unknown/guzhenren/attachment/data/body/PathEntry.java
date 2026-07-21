package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One path: Qi Path, Grandmaster, 1000 marks, 200 specks. mark and speck are two independent counts, never
//  converted into each other (the ratio is a future thing). Attainment moves neither.  CLAUDE.md "Qi".
public record PathEntry(GuAttainment attainment, long mark, long speck) {

    public static final PathEntry DEFAULT = new PathEntry(GuAttainment.NONE, 0L, 0L);

    //  One immortal-scale mark is 10000 mortal-scale specks. Independent counts, never auto-converted.
    //     TODO(convert): mark <-> speck at 1:10000 -- no caller yet; it needs a Gu or item to trigger it.
    public static final long MARK_PER_SPECK = 10_000L;

    public static final Codec<PathEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuAttainment.CODEC.optionalFieldOf("attainment", GuAttainment.NONE)
                    .forGetter(PathEntry::attainment),
            Codec.LONG.optionalFieldOf("mark", 0L).forGetter(PathEntry::mark),
            Codec.LONG.optionalFieldOf("speck", 0L).forGetter(PathEntry::speck)
    ).apply(instance, PathEntry::new));

    public static final StreamCodec<ByteBuf, PathEntry> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuAttainment.class), PathEntry::attainment,
            ByteBufCodecs.VAR_LONG, PathEntry::mark,
            ByteBufCodecs.VAR_LONG, PathEntry::speck,
            PathEntry::new);

    public PathEntry {
        mark = Math.max(0L, mark);
        speck = Math.max(0L, speck);
    }

    //  Indistinguishable from "absent" -- that is what lets PathData stay sparse.
    public boolean isDefault() {return attainment == GuAttainment.NONE && mark == 0L && speck == 0L;}
    public PathEntry withAttainment(GuAttainment v) {return new PathEntry(v, mark, speck);}
    public PathEntry withMark(long v) {return new PathEntry(attainment, v, speck);}
    public PathEntry withSpeck(long v) {return new PathEntry(attainment, mark, v);}
}

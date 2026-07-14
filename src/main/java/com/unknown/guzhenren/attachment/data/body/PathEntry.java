package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One path: 气道 大宗师 道痕1000. Attainment and marks never move each other.
public record PathEntry(GuAttainment attainment, long mark) {

    public static final PathEntry DEFAULT = new PathEntry(GuAttainment.NONE, 0L);

    public static final Codec<PathEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuAttainment.CODEC.optionalFieldOf("attainment", GuAttainment.NONE)
                    .forGetter(PathEntry::attainment),
            Codec.LONG.optionalFieldOf("mark", 0L).forGetter(PathEntry::mark)
    ).apply(instance, PathEntry::new));

    public static final StreamCodec<ByteBuf, PathEntry> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuAttainment.class), PathEntry::attainment,
            ByteBufCodecs.VAR_LONG, PathEntry::mark,
            PathEntry::new);

    public PathEntry {
        mark = Math.max(0L, mark);
    }

    //  Indistinguishable from "absent" -- that is what lets PathData stay sparse.
    public boolean isDefault() {return attainment == GuAttainment.NONE && mark == 0L;}
    public PathEntry withAttainment(GuAttainment v) {return new PathEntry(v, mark);}
    public PathEntry withMark(long v) {return new PathEntry(attainment, v);}
}

package com.unknown.guzhenren.attachment.data.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  One path: 气道 大宗师 道痕1000. Attainment and marks never move each other.
public record PathEntry(GuPathAttainment attainment, long mark) {

    public static final PathEntry DEFAULT = new PathEntry(GuPathAttainment.NONE, 0L);

    public static final Codec<PathEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GuPathAttainment.CODEC.optionalFieldOf("attainment", GuPathAttainment.NONE)
                    .forGetter(PathEntry::attainment),
            Codec.LONG.optionalFieldOf("mark", 0L).forGetter(PathEntry::mark)
    ).apply(instance, PathEntry::new));

    public static final StreamCodec<ByteBuf, PathEntry> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(GuPathAttainment.class), PathEntry::attainment,
            ByteBufCodecs.VAR_LONG, PathEntry::mark,
            PathEntry::new);

    public PathEntry {
        mark = Math.max(0L, mark);
    }

    //  Indistinguishable from "absent" -- that is what lets PathData stay sparse.
    public boolean isDefault() {return attainment == GuPathAttainment.NONE && mark == 0L;}
    public PathEntry withAttainment(GuPathAttainment v) {return new PathEntry(v, mark);}
    public PathEntry withMark(long v) {return new PathEntry(attainment, v);}
}

package com.unknown.guzhenren.attachment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import com.unknown.guzhenren.util.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  What a player has achieved in one single path, e.g. 气道 大宗师 道痕1000.
//  Attainment and marks are independent by design -- earning marks does not promote you, and
//  being promoted does not grant marks.
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

    //  A default entry is indistinguishable from "not in the map at all", which is what lets
    //  PathData stay sparse.
    public boolean isDefault() {return attainment == GuPathAttainment.NONE && mark == 0L;}
    public PathEntry withAttainment(GuPathAttainment v) {return new PathEntry(v, mark);}
    public PathEntry withMark(long v) {return new PathEntry(attainment, v);}
}

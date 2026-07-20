package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.codec.StreamCodec;

//  The Strength Path (力道) system: which beast strengths a body has taken. One kind grants once, ever.
//  ⚠ A set, not a counter -- the count is its size, and WHICH one you took is what refuses the second Gu.
public record StrengthData(Set<BeastStrength> strengths) {

    public static final StrengthData DEFAULT = new StrengthData(Set.of());

    public static final Codec<StrengthData> CODEC = BeastStrength.CODEC.listOf()
            .xmap(list -> new StrengthData(Set.copyOf(list)), data -> List.copyOf(data.strengths()));

    public static final StreamCodec<ByteBuf, StrengthData> STREAM_CODEC =
            ModStreamCodecs.enumSet(BeastStrength.class).map(StrengthData::new, StrengthData::strengths);

    public StrengthData {
        //  EnumSet: stable ordinal order in NBT and on the wire, whatever the caller handed in.
        EnumSet<BeastStrength> copy = EnumSet.noneOf(BeastStrength.class);
        copy.addAll(strengths);
        strengths = Collections.unmodifiableSet(copy);
    }

    public boolean has(BeastStrength beast) {return strengths.contains(beast);}
    public boolean isEmpty() {return strengths.isEmpty();}
    public StrengthData with(BeastStrength beast) {return rebuilt(beast, true);}
    public StrengthData without(BeastStrength beast) {return rebuilt(beast, false);}

    //  ⚠ Every constant is a boar today. The day one is not, this needs a filter -- not a rename.
    public int boarCount() {return strengths.size();}

    //  ⚠ noneOf + addAll, never EnumSet.copyOf: copyOf throws on an empty non-EnumSet, and an
    //  unmodifiable wrapper around one is exactly that. Same trap as QiData's EnumMap(Class).
    private StrengthData rebuilt(BeastStrength beast, boolean present) {
        EnumSet<BeastStrength> next = EnumSet.noneOf(BeastStrength.class);
        next.addAll(strengths);
        if (present) {
            next.add(beast);
        } else {
            next.remove(beast);
        }
        return new StrengthData(next);
    }
}

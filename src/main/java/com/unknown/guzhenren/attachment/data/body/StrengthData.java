package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The Strength Path [力道]. Two of its three branches have data, and the two shapes differ: which beast
//  strengths a body took (a set, one kind once ever), and how many of each Human Jun strength (a count, nine max).
//  ⚠ Neither is a mark source -- the Strength Path's specks stay in PathData  CLAUDE.md.
public record StrengthData(Set<BeastStrength> beasts, Map<HumanStrength, Integer> humanStrength) {

    public static final StrengthData DEFAULT = new StrengthData(Set.of(), Map.of());

    public static final Codec<StrengthData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BeastStrength.CODEC.listOf().optionalFieldOf("beasts", List.of())
                    .forGetter(data -> List.copyOf(data.beasts())),
            Codec.unboundedMap(HumanStrength.CODEC, Codec.INT).optionalFieldOf("human_strength", Map.of())
                    .forGetter(StrengthData::humanStrength)
    ).apply(instance, (beasts, humanStrength) -> new StrengthData(Set.copyOf(beasts), humanStrength)));

    public static final StreamCodec<ByteBuf, StrengthData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.enumSet(BeastStrength.class), StrengthData::beasts,
            ModStreamCodecs.enumMap(HumanStrength.class, ByteBufCodecs.VAR_INT), StrengthData::humanStrength,
            StrengthData::new);

    public StrengthData {
        //  EnumSet: stable ordinal order in NBT and on the wire, whatever the caller handed in.
        EnumSet<BeastStrength> takenBeasts = EnumSet.noneOf(BeastStrength.class);
        takenBeasts.addAll(beasts);
        beasts = Collections.unmodifiableSet(takenBeasts);

        //  EnumMap for the same reason. 0 is "never taken", so it is pruned; nine is the ceiling.
        Map<HumanStrength, Integer> pruned = new EnumMap<>(HumanStrength.class);
        humanStrength.forEach((kind, count) -> {
            if (count != null && count > 0) pruned.put(kind, Math.min(count, HumanStrength.MAX_PER_KIND));
        });
        humanStrength = Collections.unmodifiableMap(pruned);
    }

    public boolean has(BeastStrength beast) {return beasts.contains(beast);}
    public int humanStrengthCount(HumanStrength kind) {return humanStrength.getOrDefault(kind, 0);}
    public boolean isEmpty() {return beasts.isEmpty() && humanStrength.isEmpty();}
    public StrengthData with(BeastStrength beast) {return rebuilt(beast, true);}
    public StrengthData without(BeastStrength beast) {return rebuilt(beast, false);}

    //  ⚠ Every constant is a boar today. The day one is not, this needs a filter -- not a rename.
    public int boarCount() {return beasts.size();}

    //  Whether this branch has anything to show. Its own row appears only then.
    public boolean hasBranch(StrengthBranch branch) {
        return switch (branch) {
            case BEASTS -> !beasts.isEmpty();
            case HUMAN -> !humanStrength.isEmpty();
            //  ⚠ ENVIRONMENT has no field yet because it has no spec yet -- see StrengthBranch.
            case ENVIRONMENT -> false;
        };
    }

    //  EnumMap(Class), never EnumMap(Map) -- the latter throws on an empty non-EnumMap, and humanStrength is one.
    public StrengthData withHumanStrength(HumanStrength kind, int count) {
        Map<HumanStrength, Integer> next = new EnumMap<>(HumanStrength.class);
        next.putAll(humanStrength);
        next.put(kind, Math.max(0, count));
        return new StrengthData(beasts, next);
    }

    //  ⚠ noneOf + addAll, never EnumSet.copyOf: copyOf throws on an empty non-EnumSet, and an
    //  unmodifiable wrapper around one is exactly that. Same trap as QiData's EnumMap(Class).
    private StrengthData rebuilt(BeastStrength beast, boolean present) {
        EnumSet<BeastStrength> next = EnumSet.noneOf(BeastStrength.class);
        next.addAll(beasts);
        if (present) {
            next.add(beast);
        } else {
            next.remove(beast);
        }
        return new StrengthData(next, humanStrength);
    }
}

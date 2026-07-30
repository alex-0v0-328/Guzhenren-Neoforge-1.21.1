package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
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

        //  EnumMap for the same reason. 0 is "never taken", so it is pruned; the ceiling is the kind's own.
        Map<HumanStrength, Integer> pruned = new EnumMap<>(HumanStrength.class);
        humanStrength.forEach((kind, count) -> {
            if (count != null && count > 0) pruned.put(kind, Math.min(count, kind.getMaxLayers()));
        });
        humanStrength = Collections.unmodifiableMap(pruned);
    }

    public boolean has(BeastStrength beast) {return beasts.contains(beast);}
    public int humanStrengthCount(HumanStrength kind) {return humanStrength.getOrDefault(kind, 0);}
    public boolean isEmpty() {return beasts.isEmpty() && humanStrength.isEmpty();}
    public StrengthData with(BeastStrength beast) {return rebuilt(beast, true);}
    public StrengthData without(BeastStrength beast) {return rebuilt(beast, false);}

    //  Each beast family's reading, summed over what he took from it -- 白豕 + 黑豕 = 两猪之力.
    //  ⚠ Keyed by the SPECIES tag, which IS the family level, so a new beast needs no change here.
    public Map<MarkTag, Integer> beastReadings() {
        Map<MarkTag, Integer> readings = new EnumMap<>(MarkTag.class);
        for (BeastStrength beast : beasts) readings.merge(beast.getMarkTag(), beast.getReading(), Integer::sum);
        return readings;
    }

    //  Every kind's layers weighed in 斤 -- the one total both surfaces show. ⚠ 0..9999 by construction,
    //  and it equals jinReading() + junReading() * 30; the caps are what keep it out of a fifth digit.
    public int totalJin() {
        int total = 0;
        for (HumanStrength kind : HumanStrength.values()) total += humanStrengthCount(kind) * kind.getJin();
        return total;
    }

    //  A family's reading: the base kind's layers, plus ten a layer of its ×10 kind. 0..330 and 0..99.
    //  ⚠ Layer ARITHMETIC, not a tens digit -- 30 layers a kind stopped fitting one on 2026-07-26.
    public int junReading() {return reading(HumanStrength.JUN, HumanStrength.TEN_JUN);}
    public int jinReading() {return reading(HumanStrength.JIN, HumanStrength.TEN_JIN);}

    private int reading(HumanStrength base, HumanStrength ten) {
        return humanStrengthCount(base) + HumanStrength.TEN_FACTOR * humanStrengthCount(ten);
    }

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

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
        EnumSet<BeastStrength> takenBeasts = EnumSet.noneOf(BeastStrength.class);
        takenBeasts.addAll(beasts);
        beasts = Collections.unmodifiableSet(takenBeasts);

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

    public Map<MarkTag, Integer> beastReadings() {
        Map<MarkTag, Integer> readings = new EnumMap<>(MarkTag.class);
        for (BeastStrength beast : beasts) readings.merge(beast.getMarkTag(), beast.getReading(), Integer::sum);
        return readings;
    }

    public int totalJin() {
        int total = 0;
        for (HumanStrength kind : HumanStrength.values()) total += humanStrengthCount(kind) * kind.getJin();
        return total;
    }

    public int junReading() {return reading(HumanStrength.JUN, HumanStrength.TEN_JUN);}
    public int jinReading() {return reading(HumanStrength.JIN, HumanStrength.TEN_JIN);}

    private int reading(HumanStrength base, HumanStrength ten) {
        return humanStrengthCount(base) + HumanStrength.TEN_FACTOR * humanStrengthCount(ten);
    }

    public boolean hasBranch(StrengthBranch branch) {
        return switch (branch) {
            case BEASTS -> !beasts.isEmpty();
            case HUMAN -> !humanStrength.isEmpty();
            case ENVIRONMENT -> false;
        };
    }

    public StrengthData withHumanStrength(HumanStrength kind, int count) {
        Map<HumanStrength, Integer> next = new EnumMap<>(HumanStrength.class);
        next.putAll(humanStrength);
        next.put(kind, Math.max(0, count));
        return new StrengthData(beasts, next);
    }

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

package com.unknown.guzhenren.attachment.data.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.BeastStrengthFamily;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthPathBranch;
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

/**
 * Strength [力道]: the beast and human strengths a player has refined into themselves.
 *
 * <p>Immutable record attachment keyed {@code strength_data}; {@link
 * com.unknown.guzhenren.attachment.service.body.StrengthService} is the only writer. Two components:
 * a {@code Set} of {@link BeastStrength} (one kind once ever) and a sparse {@code Map} of
 * {@link HumanStrength} to layer count. The compact ctor caps each human kind at its own
 * {@code getMaxLayers()} and prunes zero-or-below entries.
 *
 * <p>⚠ The compact constructor must use {@code new EnumMap<>(Class)} plus {@code putAll}, never the
 * {@code EnumMap(Map)} copy constructor -- that one throws on the empty maps {@code DEFAULT} is built
 * from. Same shape for the {@link EnumSet}. ⚠ The four per-kind layer caps (9/9/30/30) sum to exactly
 * 9,999 jin; changing one constant breaks that identity. ⚠ {@code beastReadings()} groups by
 * {@code BeastStrengthFamily}, not by species constant -- two boars share one family and one bracket.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.StrengthService
 * @since 1.0.0
 */

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

    public Map<BeastStrengthFamily, Integer> beastReadings() {
        Map<BeastStrengthFamily, Integer> readings = new EnumMap<>(BeastStrengthFamily.class);
        for (BeastStrength beast : beasts) readings.merge(beast.getFamily(), beast.getReading(), Integer::sum);
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

    public boolean hasPathBranch(StrengthPathBranch branch) {
        return switch (branch) {
            case BEAST_STRENGTH_PHANTOM -> !beasts.isEmpty();
            case HUMAN_JUN_STRENGTH -> !humanStrength.isEmpty();
            case ATMOSPHERIC_HEAVEN_AND_EARTH, NORMAL -> false;
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

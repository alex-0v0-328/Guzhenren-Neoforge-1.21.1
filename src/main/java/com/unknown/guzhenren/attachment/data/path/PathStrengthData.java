package com.unknown.guzhenren.attachment.data.path;

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
 * com.unknown.guzhenren.attachment.service.path.PathStrengthService} is the only writer. Two components:
 * a {@code Set} of {@link BeastStrength} (one kind once ever) and a sparse {@code Map} of
 * {@link HumanStrength} to layer count. The compact ctor caps each entry at its own {@link
 * HumanStrength#getMaxLayers()} limit and prunes zero-or-below entries.
 *
 * <p>⚠ The compact constructor must use {@code new EnumMap<>(Class)} plus {@code putAll}, never the
 * {@code EnumMap(Map)} copy constructor -- that one throws on the empty maps {@code DEFAULT} is built
 * from. Same shape for the {@link EnumSet}. ⚠ The four per-kind layer caps (9/9/30/30) sum to exactly
 * 9,999 jin; changing one constant breaks that identity. ⚠ {@code beastReadings()} groups by
 * {@code BeastStrengthFamily}, not by species constant -- two boars share one family and one bracket.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.attachment.service.path.PathStrengthService
 * @since 1.0.0
 */

public record PathStrengthData(Set<BeastStrength> beasts, Map<HumanStrength, Integer> humanStrength) {

    public static final PathStrengthData DEFAULT = new PathStrengthData(Set.of(), Map.of());

    public static final Codec<PathStrengthData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BeastStrength.CODEC.listOf().optionalFieldOf("beasts", List.of())
                    .forGetter(data -> List.copyOf(data.beasts())),
            Codec.unboundedMap(HumanStrength.CODEC, Codec.INT).optionalFieldOf("human_strength", Map.of())
                    .forGetter(PathStrengthData::humanStrength)
    ).apply(instance, (beasts, humanStrength) -> new PathStrengthData(Set.copyOf(beasts), humanStrength)));

    public static final StreamCodec<ByteBuf, PathStrengthData> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.enumSet(BeastStrength.class), PathStrengthData::beasts,
            ModStreamCodecs.enumMap(HumanStrength.class, ByteBufCodecs.VAR_INT), PathStrengthData::humanStrength,
            PathStrengthData::new);

    public PathStrengthData {
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
    public PathStrengthData with(BeastStrength beast) {return rebuilt(beast, true);}
    public PathStrengthData without(BeastStrength beast) {return rebuilt(beast, false);}

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

    public PathStrengthData withHumanStrength(HumanStrength kind, int count) {
        Map<HumanStrength, Integer> next = new EnumMap<>(HumanStrength.class);
        next.putAll(humanStrength);
        next.put(kind, Math.max(0, count));
        return new PathStrengthData(beasts, next);
    }

    private PathStrengthData rebuilt(BeastStrength beast, boolean present) {
        EnumSet<BeastStrength> next = EnumSet.noneOf(BeastStrength.class);
        next.addAll(beasts);
        if (present) {
            next.add(beast);
        } else {
            next.remove(beast);
        }
        return new PathStrengthData(next, humanStrength);
    }
}

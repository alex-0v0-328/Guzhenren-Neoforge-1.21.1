package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * The Gu kept inside each Aperture [空窍], including the Vital Gu [本命蛊] bound to that aperture.
 *
 * <p>⚠ Serialized but NOT synced. The client never receives this, so every reader has to be server-side
 * and anything a screen needs must travel by some other route.
 *
 * @author Alex
 * @since 1.0.0
 * @see ApertureData
 */
public record ApertureStorage(List<List<ItemStack>> byAperture, List<ItemStack> vital) {

    public static final ApertureStorage DEFAULT = new ApertureStorage(List.of(), List.of());

    public static final Codec<ApertureStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().listOf().optionalFieldOf("by_aperture", List.of())
                    .forGetter(ApertureStorage::byAperture),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("vital", List.of())
                    .forGetter(ApertureStorage::vital)
    ).apply(instance, ApertureStorage::new));

    public ApertureStorage {
        List<List<ItemStack>> kept = new ArrayList<>();
        for (int i = 0; i < Math.min(byAperture.size(), ApertureData.MAX_APERTURES); i++) {
            List<ItemStack> items = new ArrayList<>(byAperture.get(i));
            while (!items.isEmpty() && items.getLast().isEmpty()) items.removeLast();
            kept.add(Collections.unmodifiableList(items));
        }
        while (!kept.isEmpty() && kept.getLast().isEmpty()) kept.removeLast();
        byAperture = Collections.unmodifiableList(kept);

        List<ItemStack> bound = new ArrayList<>(
                vital.subList(0, Math.min(vital.size(), ApertureData.MAX_APERTURES)));
        while (!bound.isEmpty() && bound.getLast().isEmpty()) bound.removeLast();
        vital = Collections.unmodifiableList(bound);
    }

    public List<ItemStack> get(int aperture) {
        return aperture >= 0 && aperture < byAperture.size() ? byAperture.get(aperture) : List.of();
    }

    public ItemStack getVital(int aperture) {
        return aperture >= 0 && aperture < vital.size() ? vital.get(aperture) : ItemStack.EMPTY;
    }

    public int count(int aperture) {return get(aperture).size();}
    public boolean isEmpty() {return byAperture.isEmpty() && vital.isEmpty();}

    public ApertureStorage with(int aperture, List<ItemStack> items) {
        if (aperture < 0 || aperture >= ApertureData.MAX_APERTURES) return this;

        List<List<ItemStack>> next = new ArrayList<>(byAperture);
        while (next.size() <= aperture) next.add(List.of());
        next.set(aperture, items);
        return new ApertureStorage(next, vital);
    }

    public ApertureStorage withVital(int aperture, ItemStack stack) {
        if (aperture < 0 || aperture >= ApertureData.MAX_APERTURES) return this;

        List<ItemStack> next = new ArrayList<>(vital);
        while (next.size() <= aperture) next.add(ItemStack.EMPTY);
        next.set(aperture, stack);
        return new ApertureStorage(byAperture, next);
    }
}

package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

//  What each aperture holds: a Gu list per aperture, plus the one Gu its owner bound his fate to.
//  Both are indexed the way ApertureData is. The store has no cap; the Vital Gu is one slot each.
//  ⚠ Serialized but NOT synced -- the client sees these through the container menu, on vanilla's own
//  channel. Syncing them would re-push every stack on every slot click. See CLAUDE.md "Networking".
public record ApertureStorage(List<List<ItemStack>> byAperture, List<ItemStack> vital) {

    public static final ApertureStorage DEFAULT = new ApertureStorage(List.of(), List.of());

    //  ⚠ OPTIONAL_CODEC, not CODEC: an interior empty is a real hole that must survive, or items would
    //  jump slots the moment a gap is saved. ⚠ No STREAM_CODEC on purpose -- see the header. That is
    //  also what keeps this off RegistryFriendlyByteBuf, which every other codec in this mod avoids.
    private static final Codec<ApertureStorage> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().listOf().optionalFieldOf("by_aperture", List.of())
                    .forGetter(ApertureStorage::byAperture),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("vital", List.of())
                    .forGetter(ApertureStorage::vital)
    ).apply(instance, ApertureStorage::new));

    //  TODO(migration): the pre-Vital-Gu shape was a bare list. Drop this alternative -- and the field
    //  order it forces -- once no world older than 2026-07-21 has to be opened again.
    private static final Codec<ApertureStorage> LEGACY_CODEC = ItemStack.OPTIONAL_CODEC.listOf().listOf()
            .xmap(items -> new ApertureStorage(items, List.of()), ApertureStorage::byAperture);

    public static final Codec<ApertureStorage> CODEC = Codec.withAlternative(RECORD_CODEC, LEGACY_CODEC);

    public ApertureStorage {
        //  Only TRAILING empties are trimmed, at both levels -- holes in the middle are slot positions.
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

    //  Reads never fail: an aperture nobody filled reads back as an empty list.
    public List<ItemStack> get(int aperture) {
        return aperture >= 0 && aperture < byAperture.size() ? byAperture.get(aperture) : List.of();
    }

    public ItemStack getVital(int aperture) {
        return aperture >= 0 && aperture < vital.size() ? vital.get(aperture) : ItemStack.EMPTY;
    }

    public int count(int aperture) {return get(aperture).size();}
    public boolean isEmpty() {return byAperture.isEmpty() && vital.isEmpty();}

    //  Grows to reach the index -- unlike ApertureData, storage may be written before it is "opened".
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

package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

//  What each aperture holds: a Gu list per aperture, indexed the same way ApertureData is. No cap.
//  ⚠ Serialized but NOT synced -- the client sees these through the container menu, on vanilla's own
//  channel. Syncing them would re-push every stack on every slot click. See CLAUDE.md "Networking".
public record ApertureStorage(List<List<ItemStack>> byAperture) {

    public static final ApertureStorage DEFAULT = new ApertureStorage(List.of());

    //  ⚠ OPTIONAL_CODEC, not CODEC: an interior empty is a real hole that must survive, or items would
    //  jump slots the moment a gap is saved. ⚠ No STREAM_CODEC on purpose -- see the header. That is
    //  also what keeps this off RegistryFriendlyByteBuf, which every other codec in this mod avoids.
    public static final Codec<ApertureStorage> CODEC = ItemStack.OPTIONAL_CODEC.listOf().listOf()
            .xmap(ApertureStorage::new, ApertureStorage::byAperture);

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
    }

    //  Reads never fail: an aperture nobody filled reads back as an empty list.
    public List<ItemStack> get(int aperture) {
        return aperture >= 0 && aperture < byAperture.size() ? byAperture.get(aperture) : List.of();
    }

    public int count(int aperture) {return get(aperture).size();}
    public boolean isEmpty() {return byAperture.isEmpty();}

    //  Grows to reach the index -- unlike ApertureData, storage may be written before it is "opened".
    public ApertureStorage with(int aperture, List<ItemStack> items) {
        if (aperture < 0 || aperture >= ApertureData.MAX_APERTURES) return this;

        List<List<ItemStack>> next = new ArrayList<>(byAperture);
        while (next.size() <= aperture) next.add(List.of());
        next.set(aperture, items);
        return new ApertureStorage(next);
    }
}

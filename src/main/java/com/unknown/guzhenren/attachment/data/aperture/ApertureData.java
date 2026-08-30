package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The Aperture [空窍] attachment: a mortal has none, and awakening [开窍] is what puts one here. Immutable
 * record attachment keyed {@code aperture_data}, synced {@code OWNER_ONLY}, written only by {@link
 * com.unknown.guzhenren.attachment.service.aperture.ApertureService}; holds up to {@code MAX_APERTURES}
 * (2) {@link Aperture} entries -- an empty list IS the "unawakened" state, not a special flag.
 *
 * <p>⚠ {@code with(index, aperture)} REFUSES to grow the list -- only {@code opened} appends -- the
 * mirror of {@link ApertureStorage#with}, which GROWS to reach its index; deliberately opposite. ⚠
 * {@code MAX_APERTURES} caps the {@code STREAM_CODEC} list too: longer is silently truncated on sync.
 *
 * @author Alex
 * @version 1.0.0
 * @see Aperture
 * @see com.unknown.guzhenren.attachment.service.aperture.ApertureService
 * @since 1.0.0
 */

public record ApertureData(List<Aperture> apertures) {

    public static final int MAX_APERTURES = 2;
    public static final int PRIMARY = 0;
    public static final int SECONDARY = 1;

    public static final ApertureData DEFAULT = new ApertureData(List.of());

    public static final Codec<ApertureData> CODEC = Aperture.CODEC.listOf()
            .xmap(ApertureData::new, ApertureData::apertures);

    public static final StreamCodec<ByteBuf, ApertureData> STREAM_CODEC =
            Aperture.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_APERTURES))
                    .map(ApertureData::new, ApertureData::apertures);
    public ApertureData {
        apertures = apertures.size() <= MAX_APERTURES
                ? List.copyOf(apertures)
                : List.copyOf(apertures.subList(0, MAX_APERTURES));
    }
    public Aperture get(int i) {return i >= 0 && i < apertures.size() ? apertures.get(i) : Aperture.NONE;}
    public Aperture primary() {return get(PRIMARY);}
    public int count() {return apertures.size();}
    public boolean isAwakened() {return !apertures.isEmpty();}
    public boolean isFull() {return apertures.size() >= MAX_APERTURES;}
    public ApertureData opened(Aperture aperture) {
        if (isFull()) return this;

        List<Aperture> next = new ArrayList<>(apertures);
        next.add(aperture);
        return new ApertureData(next);
    }
    public ApertureData with(int index, Aperture aperture) {
        if (index < 0 || index >= apertures.size()) return this;

        List<Aperture> next = new ArrayList<>(apertures);
        next.set(index, aperture);
        return new ApertureData(next);
    }
}

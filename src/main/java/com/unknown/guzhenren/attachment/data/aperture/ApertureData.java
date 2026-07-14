package com.unknown.guzhenren.attachment.data.aperture;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//  The aperture (空窍) system: 0..2 of them. 未开窍 IS the empty list -- that is the whole point of
//  storing a list rather than flat fields. ⚠ 第二空窍 caps at two, by the world's own rule.
public record ApertureData(List<Aperture> apertures) {

    public static final int MAX_APERTURES = 2;
    public static final int PRIMARY = 0;

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

    //  Reads never fail: an index nobody opened reads back as NONE (凡人 / 未觉醒 / 真元 0).
    public Aperture get(int i) {return i >= 0 && i < apertures.size() ? apertures.get(i) : Aperture.NONE;}
    public Aperture primary() {return get(PRIMARY);}
    public int count() {return apertures.size();}
    public boolean isAwakened() {return !apertures.isEmpty();}
    public boolean isFull() {return apertures.size() >= MAX_APERTURES;}

    //  开窍: appends. A full holder keeps what he has -- the caller is what refuses, with a reason.
    public ApertureData opened(Aperture aperture) {
        if (isFull()) return this;

        List<Aperture> next = new ArrayList<>(apertures);
        next.add(aperture);
        return new ApertureData(next);
    }

    //  Replaces an aperture that exists. Writing to an index nobody opened is a no-op, never a grow.
    public ApertureData with(int index, Aperture aperture) {
        if (index < 0 || index >= apertures.size()) return this;

        List<Aperture> next = new ArrayList<>(apertures);
        next.set(index, aperture);
        return new ApertureData(next);
    }
}

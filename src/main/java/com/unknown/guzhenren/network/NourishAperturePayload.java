package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client intent: begin or abandon nourishing the aperture [温养空窍].
 *
 * <p>⚠ Two intents ride one payload as a named action rather than as a bare boolean, because a
 * boolean at the call site reads as nothing at all.
 *
 * @author Alex
 * @since 1.0.0
 */
public record NourishAperturePayload(Action action) implements CustomPacketPayload {

    public enum Action {START, CANCEL}

    public static final Type<NourishAperturePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "nourish_aperture"));

    public static final StreamCodec<ByteBuf, NourishAperturePayload> STREAM_CODEC = StreamCodec.composite(
            ModStreamCodecs.ofEnum(Action.class), NourishAperturePayload::action,
            NourishAperturePayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

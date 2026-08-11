package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client intent: open the store of the named aperture [空窍].
 *
 * <p>⚠ Client intent is the one direction attachment sync cannot carry. No payload may carry player
 * data -- that always travels the other way, as synced state.
 *
 * @author Alex
 * @since 1.0.0
 */
public record OpenApertureStoragePayload(int aperture) implements CustomPacketPayload {

    public static final Type<OpenApertureStoragePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "open_aperture_storage"));

    public static final StreamCodec<ByteBuf, OpenApertureStoragePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(OpenApertureStoragePayload::new, OpenApertureStoragePayload::aperture);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//  "Open my aperture's storage." The ONLY payload this mod has, and it carries no player data --
//  just which aperture. Everything inside the container then rides vanilla's own slot channel.
//  ⚠ Do not add a second payload. Player data syncs through attachments. See CLAUDE.md "Networking".
public record OpenApertureStoragePayload(int aperture) implements CustomPacketPayload {

    public static final Type<OpenApertureStoragePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "open_aperture_storage"));

    public static final StreamCodec<ByteBuf, OpenApertureStoragePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(OpenApertureStoragePayload::new, OpenApertureStoragePayload::aperture);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

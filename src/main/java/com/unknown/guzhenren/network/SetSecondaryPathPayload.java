package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  "I picked this secondary path." The SECOND and last payload, same shape as the first: a client
//  intent, carrying no player data. Attachment sync is server->client only and cannot carry a choice
//  upstream, and the info panel is a plain Screen -- it has no container, so not even clickMenuButton.
//  ⚠ Two upstream triggers is the whole ceiling.  CLAUDE.md "Networking".
public record SetSecondaryPathPayload(int aperture, @Nullable GuPath path) implements CustomPacketPayload {

    public static final Type<SetSecondaryPathPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "set_secondary_path"));

    //  ⚠ Nullable on the wire: clearing it is a real choice, and GuPath has no NONE to spell that with.
    public static final StreamCodec<ByteBuf, SetSecondaryPathPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, SetSecondaryPathPayload::aperture,
            ModStreamCodecs.ofNullableEnum(GuPath.class), SetSecondaryPathPayload::path,
            SetSecondaryPathPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

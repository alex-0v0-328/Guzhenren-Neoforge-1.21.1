package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client intent: open the refinement [炼蛊] menu.
 *
 * @author Alex
 * @since 1.0.0
 */
public record OpenRefinementPayload() implements CustomPacketPayload {

    public static final OpenRefinementPayload INSTANCE = new OpenRefinementPayload();

    public static final Type<OpenRefinementPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "open_refinement"));

    public static final StreamCodec<ByteBuf, OpenRefinementPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

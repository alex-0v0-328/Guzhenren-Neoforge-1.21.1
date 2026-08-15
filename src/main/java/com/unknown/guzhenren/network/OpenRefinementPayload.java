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
 * <p>A zero-byte singleton payload -- it carries no data at all, only the button press. The server
 * handler in {@link com.unknown.guzhenren.network.ModPayloads} opens the
 * {@link com.unknown.guzhenren.menu.RefinementMenu}. Client intent is the one direction attachment
 * sync cannot carry; no player data travels upstream.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.network.ModPayloads
 */
public record OpenRefinementPayload() implements CustomPacketPayload {

    public static final OpenRefinementPayload INSTANCE = new OpenRefinementPayload();

    public static final Type<OpenRefinementPayload> TYPE = new Type<>(
            Guzhenren.id("open_refinement"));

    public static final StreamCodec<ByteBuf, OpenRefinementPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

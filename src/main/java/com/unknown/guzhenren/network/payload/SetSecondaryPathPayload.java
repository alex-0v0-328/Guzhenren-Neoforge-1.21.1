package com.unknown.guzhenren.network.payload;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.serialization.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client intent: choose the secondary path [辅修] on the named aperture [空窍].
 *
 * <p>A payload carrying only an aperture index and a nullable {@link com.unknown.guzhenren.custom.enums.path.GuPath}
 * -- no other player data. The server handler in {@link com.unknown.guzhenren.network.ModPayloads}
 * writes the choice through the aperture service. The path travels as its ordinal plus one (zero =
 * unset) via {@link com.unknown.guzhenren.serialization.ModStreamCodecs#ofNullableEnum}.
 *
 * <p>⚠ It is a payload only because the screen that sends it has no menu behind it. Anything inside a
 * container sends its intent over vanilla's own channels instead.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.network.ModPayloads
 * @since 1.0.0
 */

public record SetSecondaryPathPayload(int aperture, @Nullable GuPath path) implements CustomPacketPayload {

    public static final Type<SetSecondaryPathPayload> TYPE = new Type<>(
            Guzhenren.id("set_secondary_path"));

    public static final StreamCodec<ByteBuf, SetSecondaryPathPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, SetSecondaryPathPayload::aperture,
            ModStreamCodecs.ofNullableEnum(GuPath.class), SetSecondaryPathPayload::path,
            SetSecondaryPathPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

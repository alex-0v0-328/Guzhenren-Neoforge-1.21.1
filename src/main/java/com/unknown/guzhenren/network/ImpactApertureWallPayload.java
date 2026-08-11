package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client intent: strike the aperture wall [冲击窍壁] and take whatever the roll gives.
 *
 * @author Alex
 * @since 1.0.0
 */
public record ImpactApertureWallPayload() implements CustomPacketPayload {

    public static final ImpactApertureWallPayload INSTANCE = new ImpactApertureWallPayload();

    public static final Type<ImpactApertureWallPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "impact_aperture_wall"));

    public static final StreamCodec<ByteBuf, ImpactApertureWallPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record CrashStepPayload(int vertical, int horizontal, float yRot) implements CustomPacketPayload {

    public static final Type<CrashStepPayload> TYPE = new Type<>(Guzhenren.id("crash_step"));

    public static final StreamCodec<ByteBuf, CrashStepPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CrashStepPayload::vertical,
            ByteBufCodecs.INT, CrashStepPayload::horizontal,
            ByteBufCodecs.FLOAT, CrashStepPayload::yRot,
            CrashStepPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}

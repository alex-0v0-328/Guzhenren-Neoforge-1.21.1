package com.unknown.guzhenren.network;

import com.unknown.guzhenren.Guzhenren;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * The client-intent payload for one step of a Crash Gu [横冲直撞蛊] dash.
 *
 * <p>Sent by the client while a crash effect is running; carries the vertical and horizontal step
 * counts plus the facing they were taken at.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.effect.timed.CrashGuEffect
 * @since 1.0.0
 */

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

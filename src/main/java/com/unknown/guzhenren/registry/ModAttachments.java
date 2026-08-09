package com.unknown.guzhenren.registry;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.QiData;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.body.StaminaData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Guzhenren.MOD_ID);

    private static final BiPredicate<IAttachmentHolder, ServerPlayer> OWNER_ONLY =
            (holder, viewer) -> holder == viewer;

    //region Aperture [空窍]
    public static final Supplier<AttachmentType<ApertureData>> APERTURE = ATTACHMENT_TYPES.register(
            "aperture_data", () -> AttachmentType.builder(() -> ApertureData.DEFAULT)
                    .serialize(ApertureData.CODEC)
                    .sync(OWNER_ONLY, ApertureData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<float[]>> ESSENCE_CARRY = ATTACHMENT_TYPES.register(
            "essence_carry", () -> AttachmentType.builder(
                    () -> new float[ApertureData.MAX_APERTURES]).build());

    public static final Supplier<AttachmentType<ApertureStorage>> APERTURE_STORAGE = ATTACHMENT_TYPES.register(
            "aperture_storage", () -> AttachmentType.builder(() -> ApertureStorage.DEFAULT)
                    .serialize(ApertureStorage.CODEC)
                    .build());
    //endregion

    //region Body [肉身]
    public static final Supplier<AttachmentType<BodyData>> BODY = ATTACHMENT_TYPES.register(
            "body_data", () -> AttachmentType.builder(() -> BodyData.DEFAULT)
                    .serialize(BodyData.CODEC)
                    .sync(OWNER_ONLY, BodyData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<SoulData>> SOUL = ATTACHMENT_TYPES.register(
            "soul_data", () -> AttachmentType.builder(() -> SoulData.DEFAULT)
                    .serialize(SoulData.CODEC)
                    .sync(OWNER_ONLY, SoulData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<StaminaData>> STAMINA = ATTACHMENT_TYPES.register(
            "stamina_data", () -> AttachmentType.builder(() -> StaminaData.DEFAULT)
                    .serialize(StaminaData.CODEC)
                    .sync(OWNER_ONLY, StaminaData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<PathData>> PATH = ATTACHMENT_TYPES.register(
            "path_data", () -> AttachmentType.builder(() -> PathData.DEFAULT)
                    .serialize(PathData.CODEC)
                    .sync(OWNER_ONLY, PathData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<StrengthData>> STRENGTH = ATTACHMENT_TYPES.register(
            "strength_data", () -> AttachmentType.builder(() -> StrengthData.DEFAULT)
                    .serialize(StrengthData.CODEC)
                    .sync(OWNER_ONLY, StrengthData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<QiData>> QI = ATTACHMENT_TYPES.register(
            "qi_data", () -> AttachmentType.builder(() -> QiData.DEFAULT)
                    .serialize(QiData.CODEC)
                    .sync(OWNER_ONLY, QiData.STREAM_CODEC)
                    .build());
    //endregion

    //region Mind [脑海]
    public static final Supplier<AttachmentType<MindData>> MIND = ATTACHMENT_TYPES.register(
            "mind_data", () -> AttachmentType.builder(() -> MindData.DEFAULT)
                    .serialize(MindData.CODEC)
                    .sync(OWNER_ONLY, MindData.STREAM_CODEC)
                    .build());
    //endregion

    public static final Supplier<AttachmentType<Boolean>> BORN = ATTACHMENT_TYPES.register(
            "born_flag", () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .serialize(Codec.BOOL)
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

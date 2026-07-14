package com.unknown.guzhenren.registry;

import com.mojang.serialization.Codec;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.QiData;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

//  空窍 / 肉身 / 脑海 -- three domains, six attachments: soul, path and qi carry a pool and two maps, so they
//  sync on their own clock instead of riding along with the body. Write only through attachment/service.
//  .sync() is why this mod has no packets -- see CLAUDE.md "Networking".
public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Guzhenren.MOD_ID);

    //  Without this, NeoForge syncs to everyone who can see the holder. To reveal rank later
    //  (观气术, name tags): loosen APERTURE's predicate. See CLAUDE.md "Networking".
    private static final BiPredicate<IAttachmentHolder, ServerPlayer> OWNER_ONLY =
            (holder, viewer) -> holder == viewer;

    //region 空窍
    //  No copyOnDeath: onClone is the single source of truth for what a clone inherits --
    //  a death copy and a reset can't both be the last word, so only one place may write.
    public static final Supplier<AttachmentType<ApertureData>> APERTURE = ATTACHMENT_TYPES.register(
            "aperture", () -> AttachmentType.builder(() -> ApertureData.DEFAULT)
                    .serialize(ApertureData.CODEC)
                    .sync(OWNER_ONLY, ApertureData.STREAM_CODEC)
                    .build());

    //  Sub-integer remainder of essence regen, one cell per aperture. Neither serialized nor synced,
    //  and mutated in place -- see CLAUDE.md "Networking".
    public static final Supplier<AttachmentType<float[]>> ESSENCE_CARRY = ATTACHMENT_TYPES.register(
            "essence_carry", () -> AttachmentType.<float[]>builder(
                    () -> new float[ApertureData.MAX_APERTURES]).build());
    //endregion

    //region 肉身
    public static final Supplier<AttachmentType<BodyData>> BODY = ATTACHMENT_TYPES.register(
            "body", () -> AttachmentType.builder(() -> BodyData.DEFAULT)
                    .serialize(BodyData.CODEC)
                    .sync(OWNER_ONLY, BodyData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<SoulData>> SOUL = ATTACHMENT_TYPES.register(
            "soul", () -> AttachmentType.builder(() -> SoulData.DEFAULT)
                    .serialize(SoulData.CODEC)
                    .sync(OWNER_ONLY, SoulData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<PathData>> PATH = ATTACHMENT_TYPES.register(
            "path", () -> AttachmentType.builder(() -> PathData.DEFAULT)
                    .serialize(PathData.CODEC)
                    .sync(OWNER_ONLY, PathData.STREAM_CODEC)
                    .build());

    //  ⚠ Uncapped, and QiData.total() IS the 气道's path marks -- PATH stores no copy. See CLAUDE.md "Qi".
    public static final Supplier<AttachmentType<QiData>> QI = ATTACHMENT_TYPES.register(
            "qi", () -> AttachmentType.builder(() -> QiData.DEFAULT)
                    .serialize(QiData.CODEC)
                    .sync(OWNER_ONLY, QiData.STREAM_CODEC)
                    .build());
    //endregion

    //region 脑海
    //  Synced though no HUD reads it yet -- player data like the rest, ready for a 脑海 screen.
    public static final Supplier<AttachmentType<MindData>> MIND = ATTACHMENT_TYPES.register(
            "mind", () -> AttachmentType.builder(() -> MindData.DEFAULT)
                    .serialize(MindData.CODEC)
                    .sync(OWNER_ONLY, MindData.STREAM_CODEC)
                    .build());
    //endregion

    //  ⚠ Has this player ever been born? Serialized, never synced -- vanilla has no "first join" signal,
    //  and 才情 is rolled exactly once, at birth. See CLAUDE.md "Birth".
    public static final Supplier<AttachmentType<Boolean>> BORN = ATTACHMENT_TYPES.register(
            "born", () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .serialize(Codec.BOOL)
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

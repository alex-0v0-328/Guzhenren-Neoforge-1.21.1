package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.path.PathData;
import com.unknown.guzhenren.attachment.data.SoulData;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

//  The six player-data systems, plus ESSENCE_CARRY.
//  Read anywhere, client included; write only through attachment/service.
//  .sync() is why this mod has no packets -- see CLAUDE.md "Networking".
public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Guzhenren.MOD_ID);

    //  Without this, NeoForge ships the data to everyone who can see the holder.
    //  To reveal rank to others later (观气术, name tags): loosen CORE's predicate, nothing else moves.
    private static final BiPredicate<IAttachmentHolder, ServerPlayer> OWNER_ONLY =
            (holder, viewer) -> holder == viewer;

    //  No copyOnDeath: PlayerDataEvents.onClone is the single source of truth for what a clone
    //  inherits -- it copies, or resets to a mortal when a death meets keepInventory=false. A death
    //  copy and a reset cannot both be the last word, so only one place may write.
    public static final Supplier<AttachmentType<CoreData>> CORE = ATTACHMENT_TYPES.register(
            "core", () -> AttachmentType.builder(() -> CoreData.DEFAULT)
                    .serialize(CoreData.CODEC)
                    .sync(OWNER_ONLY, CoreData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<EssenceData>> ESSENCE = ATTACHMENT_TYPES.register(
            "essence", () -> AttachmentType.builder(() -> EssenceData.DEFAULT)
                    .serialize(EssenceData.CODEC)
                    .sync(OWNER_ONLY, EssenceData.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<LifespanData>> LIFESPAN = ATTACHMENT_TYPES.register(
            "lifespan", () -> AttachmentType.builder(() -> LifespanData.DEFAULT)
                    .serialize(LifespanData.CODEC)
                    .sync(OWNER_ONLY, LifespanData.STREAM_CODEC)
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

    //  Synced although no HUD reads it yet: it is player data like the other five, and a 脑海 screen
    //  later should not have to remember to come back here.
    public static final Supplier<AttachmentType<MindData>> MIND = ATTACHMENT_TYPES.register(
            "mind", () -> AttachmentType.builder(() -> MindData.DEFAULT)
                    .serialize(MindData.CODEC)
                    .sync(OWNER_ONLY, MindData.STREAM_CODEC)
                    .build());

    //  Sub-integer remainder of essence regen. Neither serialized nor synced, on purpose -- see
    //  CLAUDE.md "Networking". Losing it on relog costs under one point.
    public static final Supplier<AttachmentType<Float>> ESSENCE_CARRY = ATTACHMENT_TYPES.register(
            "essence_carry", () -> AttachmentType.builder(() -> 0.0F).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

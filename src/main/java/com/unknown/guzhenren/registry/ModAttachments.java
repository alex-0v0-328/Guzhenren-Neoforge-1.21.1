package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.attachment.data.PathData;
import com.unknown.guzhenren.attachment.data.SoulData;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

//  The five player-data systems, plus one piece of transient bookkeeping.
//
//  Read with player.getData(...) on either side, client included. Only ever *write* through the
//  matching service in attachment/service -- the services are what keep the derived values coherent.
//
//  .sync() is why this mod has no packets at all. See CLAUDE.md "Networking".
public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Guzhenren.MOD_ID);

    //  Without this predicate NeoForge ships the data to every player who can see the holder.
    //  To reveal rank to others later (观气术, name tags), give CORE a laxer predicate -- that is the
    //  entire change, nothing outside this file moves.
    private static final BiPredicate<IAttachmentHolder, ServerPlayer> OWNER_ONLY =
            (holder, viewer) -> holder == viewer;

    //  copyOnDeath is belt-and-braces: PlayerDataEvents.onClone copies all five explicitly anyway.
    public static final Supplier<AttachmentType<CoreData>> CORE = ATTACHMENT_TYPES.register(
            "core", () -> AttachmentType.builder(() -> CoreData.DEFAULT)
                    .serialize(CoreData.CODEC)
                    .sync(OWNER_ONLY, CoreData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<EssenceData>> ESSENCE = ATTACHMENT_TYPES.register(
            "essence", () -> AttachmentType.builder(() -> EssenceData.DEFAULT)
                    .serialize(EssenceData.CODEC)
                    .sync(OWNER_ONLY, EssenceData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<LifespanData>> LIFESPAN = ATTACHMENT_TYPES.register(
            "lifespan", () -> AttachmentType.builder(() -> LifespanData.DEFAULT)
                    .serialize(LifespanData.CODEC)
                    .sync(OWNER_ONLY, LifespanData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<SoulData>> SOUL = ATTACHMENT_TYPES.register(
            "soul", () -> AttachmentType.builder(() -> SoulData.DEFAULT)
                    .serialize(SoulData.CODEC)
                    .sync(OWNER_ONLY, SoulData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<PathData>> PATH = ATTACHMENT_TYPES.register(
            "path", () -> AttachmentType.builder(() -> PathData.DEFAULT)
                    .serialize(PathData.CODEC)
                    .sync(OWNER_ONLY, PathData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    //  The sub-integer remainder of essence regen. Neither serialized nor synced, and that is the
    //  whole point -- see CLAUDE.md "Networking". Losing it on relog costs under one point of essence.
    public static final Supplier<AttachmentType<Float>> ESSENCE_CARRY = ATTACHMENT_TYPES.register(
            "essence_carry", () -> AttachmentType.builder(() -> 0.0F).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

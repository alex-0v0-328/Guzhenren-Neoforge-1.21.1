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
//  Read with player.getData(ModAttachments.CORE) -- on either side, the client included. Only ever
//  *write* through the matching service in attachment/service: the services are what keep the
//  derived values coherent.
//
//  Sync is NeoForge's, not ours. Declaring .sync() means:
//    - setData() pushes the new value to the client by itself (Entity#setData);
//    - login, respawn and dimension change each re-send the full set by themselves -- vanilla is
//      patched to call AttachmentSync.syncInitialPlayerAttachments at all three.
//  So there is no payload, no packet handler, and no client-side mirror cache to keep in step.
public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Guzhenren.MOD_ID);

    //  A cultivator's own business. Without this predicate NeoForge ships the data to every player
    //  who can see the holder.
    //  To reveal rank to others later (观气术, name tags), give CORE a laxer predicate -- that is the
    //  entire change, nothing outside this file moves.
    private static final BiPredicate<IAttachmentHolder, ServerPlayer> OWNER_ONLY =
            (holder, viewer) -> holder == viewer;

    //  copyOnDeath is belt-and-braces: PlayerDataEvents.onClone copies all five explicitly so the
    //  behavior does not depend on how NeoForge happens to treat non-death clones (End portal
    //  return, /debug respawn) in any given version.
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

    //  The sub-integer remainder of essence regen.
    //
    //  Neither serialized nor synced, and that is the whole point. A 丁等 cultivator at 一转初阶
    //  regens well under one point per second, so the fraction must be carried somewhere -- but if
    //  it lived inside EssenceData, every regen step would setData and therefore push a packet, even
    //  on the steps where the pool did not actually move. An unsynced attachment no-ops inside
    //  setData (AttachmentSync.syncEntityUpdate returns early when syncHandler is null), so this
    //  costs nothing. Losing it on relog costs the player less than one point of essence.
    public static final Supplier<AttachmentType<Float>> ESSENCE_CARRY = ATTACHMENT_TYPES.register(
            "essence_carry", () -> AttachmentType.builder(() -> 0.0F).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageTick;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

//  The heartbeat: aging, essence and thought regen, lethal checks.
//  Sleep recovery is an edge, not a level -- see PlayerDataEvents.onWakeUp.
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        //  Per-second, not per-tick: aging only needs a day boundary, regen carries its own remainder.
        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        //  A Gu goes hungry on the same day rollover that ages its owner -- one clock, two readers:
        //  what he carries starves, what an aperture holds gets fed from his pack first.
        long days = BodyService.tickAging(player);
        if (days > 0L) {
            RefinableGuItem.starveAll(player, days);
            ApertureStorageTick.tickDay(player, days);

            //  ⚠ The tick wrote the attachment behind an open menu's back, and that menu still holds
            //  load()-time copies -- its next save would resurrect what just starved. The check lives
            //  here, not in the service: a service reaching into menu/ inverts the one strict direction.
            if (player.containerMenu instanceof ApertureStorageMenu menu) menu.reload();
        }

        closeDistilling(player);
        EssenceService.regenStep(player);
        MindService.regenStep(player);
        checkLethalState(player);
    }

    //  Phase 3's close: whatever he never spent pays back at 1:2, and the distilled pool [精炼真元] empties.
    //  ⚠⚠ A LEVEL, not an edge, and it has to be: 1.21.1's MobEffect has no expiry hook at all, so there
    //  is no edge to hang this on. Reading the level also catches milk, /effect clear and death -- none of
    //  which would have fired an expiry hook even if one existed.
    //  ⚠ Runs BEFORE regenStep, so the tick the effect ends on already regenerates into the ordinary pool.
    private static void closeDistilling(ServerPlayer player) {
        if (EssenceService.distilledEssence(player) > 0L && !EssenceService.isDistilling(player)) {
            EssenceService.endDistilling(player);
        }
    }

    //  Lifespan gone, soul collapsed, or Mind Ocean burst. onRespawn stops the death loop.
    private static void checkLethalState(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (BodyService.get(player).isExhausted()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.LIFESPAN_EXHAUSTED), Float.MAX_VALUE);
            return;
        }
        if (SoulService.get(player).isCollapsed()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.SOUL_COLLAPSE), Float.MAX_VALUE);
            return;
        }
        if (MindService.get(player).isOverflowing()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.MIND_OCEAN_SHATTERED), Float.MAX_VALUE);
        }
    }
}

package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.attachment.data.PathData;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  Cross-system operations -- the things that touch more than one attachment at once.
//  Event handlers call these and stay dumb.
public final class PlayerDataService {

    private PlayerDataService() {}

    //  Respawning with 0 lifespan would kill you again on the next tick, forever. One year of grace
    //  gives the player an in-game day to go find whatever will actually extend their life.
    public static final long GRACE_LIFESPAN = 1L;

    //  A completed night's sleep: soul back to full, essence back to full.
    public static void onSleepComplete(ServerPlayer player) {
        SoulService.refill(player);
        EssenceService.refill(player);
    }

    //  Death does not reset a cultivator -- everything is copyOnDeath. But the two conditions that
    //  can *cause* death must not still hold when the player comes back, or they die in a loop.
    public static void onRespawn(ServerPlayer player) {
        if (LifespanService.get(player).isExhausted()) {
            LifespanService.setLifespan(player, GRACE_LIFESPAN);
        }
        if (SoulService.get(player).isCollapsed()) {
            SoulService.refill(player);
        }
    }

    //  Non-death clones (End portal return, /debug respawn) and death clones both land here.
    //  Copying all five explicitly means the behavior does not depend on how any given NeoForge
    //  version happens to treat copyOnDeath for the non-death case.
    public static void copy(Player from, Player to) {
        to.setData(ModAttachments.CORE, from.getData(ModAttachments.CORE));
        to.setData(ModAttachments.ESSENCE, from.getData(ModAttachments.ESSENCE));
        to.setData(ModAttachments.LIFESPAN, from.getData(ModAttachments.LIFESPAN));
        to.setData(ModAttachments.SOUL, from.getData(ModAttachments.SOUL));
        to.setData(ModAttachments.PATH, from.getData(ModAttachments.PATH));
    }

    public static void resetAll(ServerPlayer player) {
        player.setData(ModAttachments.CORE, CoreData.DEFAULT);
        player.setData(ModAttachments.ESSENCE, EssenceData.DEFAULT);
        player.setData(ModAttachments.SOUL, SoulData.DEFAULT);
        player.setData(ModAttachments.PATH, PathData.DEFAULT);
        player.setData(ModAttachments.ESSENCE_CARRY, 0.0F);

        //  Keep the day anchor. Resetting it to UNTRACKED would just be re-adopted on the next tick,
        //  so this only avoids a pointless one-tick window where the player has no anchor.
        player.setData(ModAttachments.LIFESPAN,
                LifespanData.DEFAULT.withLastDayIndex(LifespanService.get(player).lastDayIndex()));
    }
}

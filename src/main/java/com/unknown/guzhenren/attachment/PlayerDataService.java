package com.unknown.guzhenren.attachment;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.path.PathData;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.MindService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  Cross-system operations. Event handlers call these and stay dumb.
public final class PlayerDataService {

    private PlayerDataService() {}

    //  Completed night's sleep: soul + essence to full, 念 restored -- see MindPool.slept.
    public static void onSleepComplete(ServerPlayer player) {
        SoulService.refill(player);
        EssenceService.refill(player);
        MindService.onSleepComplete(player);
    }

    //  What a fresh clone inherits. Death without keepInventory resets to mortal; else copy.
    public static void onClone(Player from, Player to, boolean wasDeath, boolean keepInventory) {
        if (wasDeath && !keepInventory) {
            resetAll(to);
        } else {
            copy(from, to);
        }
    }

    //  Clear every lethal condition, or the player dies in a respawn loop. See CLAUDE.md "Time, sleep, death".
    public static void onRespawn(ServerPlayer player) {
        if (LifespanService.get(player).isExhausted()) {
            LifespanService.setLifespan(player, LifespanData.DEFAULT_LIFESPAN);
        }
        if (SoulService.get(player).isCollapsed()) {
            SoulService.refill(player);
        }
        if (MindService.get(player).isOverflowing()) {
            MindService.clamp(player);
        }
    }

    //  Death clones and non-death clones (End portal return, /debug respawn) alike.
    public static void copy(Player from, Player to) {
        to.setData(ModAttachments.CORE, from.getData(ModAttachments.CORE));
        to.setData(ModAttachments.ESSENCE, from.getData(ModAttachments.ESSENCE));
        to.setData(ModAttachments.LIFESPAN, from.getData(ModAttachments.LIFESPAN));
        to.setData(ModAttachments.SOUL, from.getData(ModAttachments.SOUL));
        to.setData(ModAttachments.PATH, from.getData(ModAttachments.PATH));
        to.setData(ModAttachments.MIND, from.getData(ModAttachments.MIND));
    }

    //  Player, not ServerPlayer, so onClone can reset the fresh clone entity as well as a live one.
    public static void resetAll(Player player) {
        player.setData(ModAttachments.CORE, CoreData.DEFAULT);
        player.setData(ModAttachments.ESSENCE, EssenceData.DEFAULT);
        player.setData(ModAttachments.SOUL, SoulData.DEFAULT);
        player.setData(ModAttachments.PATH, PathData.DEFAULT);
        player.setData(ModAttachments.MIND, MindData.DEFAULT);
        player.setData(ModAttachments.ESSENCE_CARRY, 0.0F);

        //  Keep the day anchor: resetting it would just be re-adopted on the next tick.
        player.setData(ModAttachments.LIFESPAN,
                LifespanData.DEFAULT.withLastDayIndex(LifespanService.get(player).lastDayIndex()));
    }
}

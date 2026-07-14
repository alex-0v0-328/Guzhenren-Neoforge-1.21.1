package com.unknown.guzhenren.attachment;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.path.PathData;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.attachment.service.CoreService;
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

    //  First login ever, and nothing else -- vanilla gives no such signal, so BORN is the memory of it.
    public static void onJoin(ServerPlayer player) {
        if (!player.getData(ModAttachments.BORN)) onBirth(player);
    }

    //  ⚠ Everything a player is dealt once, at birth. 才情 is rolled HERE, not at 开窍 -- awaken must
    //  never touch it. A full reset is a rebirth, so resetAll calls this too. See CLAUDE.md "Birth".
    public static void onBirth(Player player) {
        player.setData(ModAttachments.MIND, MindData.newborn());
        player.setData(ModAttachments.BORN, true);
    }

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

    //  Un-fire every lethal condition, or the player dies in a respawn loop. What ran out comes back
    //  bare, not full: the cap survives, the contents do not. See CLAUDE.md "Time, sleep, death".
    public static void onRespawn(ServerPlayer player) {
        if (LifespanService.get(player).isExhausted()) {
            LifespanService.setLifespan(player, LifespanData.DEFAULT_LIFESPAN);
        }
        if (SoulService.get(player).isCollapsed()) {
            SoulService.revive(player);
        }
        if (MindService.get(player).isOverflowing()) {
            MindService.empty(player);
        }
    }

    //  Death clones and non-death clones (End portal return, /debug respawn) alike.
    //  BORN travels with them, or the next login would roll a second 才情 over the one he already has.
    public static void copy(Player from, Player to) {
        to.setData(ModAttachments.CORE, from.getData(ModAttachments.CORE));
        to.setData(ModAttachments.ESSENCE, from.getData(ModAttachments.ESSENCE));
        to.setData(ModAttachments.LIFESPAN, from.getData(ModAttachments.LIFESPAN));
        to.setData(ModAttachments.SOUL, from.getData(ModAttachments.SOUL));
        to.setData(ModAttachments.PATH, from.getData(ModAttachments.PATH));
        to.setData(ModAttachments.MIND, from.getData(ModAttachments.MIND));
        to.setData(ModAttachments.BORN, from.getData(ModAttachments.BORN));
    }

    //  Player, not ServerPlayer, so onClone can reset the fresh clone entity as well as a live one.
    //  A reset is a rebirth: onBirth is what writes MIND here, and it rolls a fresh 才情.
    public static void resetAll(Player player) {
        player.setData(ModAttachments.CORE, CoreData.DEFAULT);
        player.setData(ModAttachments.ESSENCE, EssenceData.DEFAULT);
        player.setData(ModAttachments.SOUL, SoulData.DEFAULT);
        player.setData(ModAttachments.PATH, PathData.DEFAULT);
        player.setData(ModAttachments.ESSENCE_CARRY, 0.0F);
        onBirth(player);

        //  Keep the day anchor: resetting it would just be re-adopted on the next tick.
        player.setData(ModAttachments.LIFESPAN,
                LifespanData.DEFAULT.withLastDayIndex(LifespanService.get(player).lastDayIndex()));
    }
}

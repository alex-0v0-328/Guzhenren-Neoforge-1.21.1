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

    //  A completed night's sleep: soul and essence to full, and 念 restored (意/情 do not recover).
    //  The 念 refill is halved if its buffer was used that day -- see MindPool.slept.
    public static void onSleepComplete(ServerPlayer player) {
        SoulService.refill(player);
        EssenceService.refill(player);
        MindService.onSleepComplete(player);
    }

    //  What a fresh clone inherits, and the only place that decides it. A dimension return (not a
    //  death) always keeps the data; a death keeps it only when keepInventory does -- otherwise
    //  cultivation dies with the inventory, back to a mortal.
    public static void onClone(Player from, Player to, boolean wasDeath, boolean keepInventory) {
        if (wasDeath && !keepInventory) {
            resetAll(to);
        } else {
            copy(from, to);
        }
    }

    //  Every condition that can *cause* death must be false when the player comes back, or they die in
    //  a loop. Dying of old age hands back a full default lifespan -- the thing that ran out is the
    //  thing that resets. Only reached when the clone kept its data (keepInventory); a reset already
    //  left every pool at a safe default.
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

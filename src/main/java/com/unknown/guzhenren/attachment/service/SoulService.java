package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.custom.enums.core.GuSoulTier;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The soul (魂魄) system.
//
//  maxSoul carries the title -- 一人魂 at 100, 十人魂 at 1000, and so on. currentSoul is what
//  abilities actually spend, and it refills to full on a completed sleep.
//  Draining it to zero is fatal: PlayerTickHandler watches for that.
public final class SoulService {

    private SoulService() {}

    //  ---- read ----
    public static SoulData get(Player player) {return player.getData(ModAttachments.SOUL);}
    public static GuSoulTier tier(Player player) {return get(player).tier();}

    //  ---- write ----
    //  SoulData's compact constructor re-clamps current against max, so lowering max cannot leave a
    //  player holding more soul than they have.
    public static void setMax(ServerPlayer p, long v) {store(p, get(p).withMaxSoul(v));}
    public static void addMax(ServerPlayer p, long delta) {setMax(p, get(p).maxSoul() + delta);}
    public static void setCurrent(ServerPlayer p, long v) {store(p, get(p).withCurrentSoul(v));}
    public static void addCurrent(ServerPlayer p, long delta) {setCurrent(p, get(p).currentSoul() + delta);}
    public static void refill(ServerPlayer p) {store(p, get(p).refilled());}
    private static void store(ServerPlayer p, SoulData data) {p.setData(ModAttachments.SOUL, data);}

    //  Spend soul, all or nothing. Returns false and changes nothing if the pool is short.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        SoulData soul = get(player);
        if (soul.currentSoul() < amount) return false;
        setCurrent(player, soul.currentSoul() - amount);
        return true;
    }
}

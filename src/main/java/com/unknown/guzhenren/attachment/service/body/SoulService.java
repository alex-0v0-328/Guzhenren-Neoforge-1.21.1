package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.custom.enums.soul.SoulTier;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The soul (魂魄) system. Refills on a completed sleep; draining it to zero is fatal.
public final class SoulService {

    private SoulService() {}

    //  ---- read ----
    public static SoulData get(Player player) {return player.getData(ModAttachments.SOUL);}
    public static SoulTier tier(Player player) {return get(player).tier();}

    //  ---- write ----
    //  SoulData re-clamps current against max, so lowering max cannot leave a player over their cap.
    public static void setMax(ServerPlayer p, long v) {store(p, get(p).withMaxSoul(v));}
    public static void addMax(ServerPlayer p, long delta) {setMax(p, get(p).maxSoul() + delta);}
    public static void setCurrent(ServerPlayer p, long v) {store(p, get(p).withCurrentSoul(v));}
    public static void addCurrent(ServerPlayer p, long delta) {setCurrent(p, get(p).currentSoul() + delta);}
    public static void refill(ServerPlayer p) {store(p, get(p).refilled());}
    public static void revive(ServerPlayer p) {store(p, get(p).revived());}
    private static void store(ServerPlayer p, SoulData data) {p.setData(ModAttachments.SOUL, data);}

    //  Spend, all or nothing.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        SoulData soul = get(player);
        if (soul.currentSoul() < amount) return false;
        setCurrent(player, soul.currentSoul() - amount);
        return true;
    }
}

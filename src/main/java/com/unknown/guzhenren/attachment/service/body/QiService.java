package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.QiData;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The qi (气) system. No cap, no lethal state: a body carries what it carries.
//  ⚠ total() is the 气道's path marks -- PathService reads it, PathData never stores it.
public final class QiService {

    private QiService() {}

    //  ---- read ----
    public static QiData get(Player p) {return p.getData(ModAttachments.QI);}
    public static long mark(Player p, QiType type) {return get(p).mark(type);}
    public static long total(Player p) {return get(p).total();}

    //  ---- write ----
    public static void setMark(ServerPlayer p, QiType t, long v) {store(p, get(p).with(t, v));}
    public static void addMark(ServerPlayer p, QiType t, long d) {setMark(p, t, mark(p, t) + d);}
    private static void store(ServerPlayer p, QiData data) {p.setData(ModAttachments.QI, data);}

    //  Spend, all or nothing. 气 leaves the body to become a 蛊材, so the 气道道痕 drop with it.
    //  addMark is the return trip (a 蛊材 spent back into the body). Ratio unbuilt -- CLAUDE.md "Qi".
    public static boolean consume(ServerPlayer player, QiType type, long amount) {
        if (amount <= 0L) return true;
        long current = mark(player, type);
        if (current < amount) return false;
        setMark(player, type, current - amount);
        return true;
    }
}

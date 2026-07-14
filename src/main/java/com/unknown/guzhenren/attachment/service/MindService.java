package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The mind (智道) system: 念 / 意 / 情 in the 脑海. No regen, only 念 recovers on sleep. See CLAUDE.md "Wisdom".
public final class MindService {

    private MindService() {}

    //  ---- read ----
    public static MindData get(Player p) {return p.getData(ModAttachments.MIND);}
    public static MindPool pool(Player p, GuWisdomType t) {return get(p).pool(t);}
    public static long current(Player p, GuWisdomType t) {return pool(p, t).current();}
    public static long max(Player p, GuWisdomType t) {return pool(p, t).max();}

    //  ---- write ----
    public static void setCurrent(ServerPlayer p, GuWisdomType t, long v) {set(p, t, pool(p, t).withCurrent(v));}
    public static void addCurrent(ServerPlayer p, GuWisdomType t, long d) {setCurrent(p, t, current(p, t) + d);}
    public static void setMax(ServerPlayer p, GuWisdomType t, long v) {set(p, t, pool(p, t).withMax(v));}
    public static void addMax(ServerPlayer p, GuWisdomType t, long d) {setMax(p, t, max(p, t) + d);}
    public static void clamp(ServerPlayer p) {store(p, get(p).clamped());}
    private static void set(ServerPlayer p, GuWisdomType t, MindPool v) {store(p, get(p).with(t, v));}
    private static void store(ServerPlayer p, MindData d) {p.setData(ModAttachments.MIND, d);}

    //  Operator tool: a clean full pool at the cap (never over, so never lethal, and no fatigue flag).
    public static void refill(ServerPlayer p, GuWisdomType t) {
        long cap = max(p, t);
        set(p, t, new MindPool(cap, cap, false));
    }

    //  A completed sleep restores 念 only -- 意/情 do not recover. See MindPool.slept.
    public static void onSleepComplete(ServerPlayer p) {
        set(p, GuWisdomType.THOUGHTS, pool(p, GuWisdomType.THOUGHTS).slept());
    }

    //  Spend, all or nothing.
    public static boolean consume(ServerPlayer player, GuWisdomType type, long amount) {
        if (amount <= 0L) return true;
        long current = current(player, type);
        if (current < amount) return false;
        setCurrent(player, type, current - amount);
        return true;
    }
}

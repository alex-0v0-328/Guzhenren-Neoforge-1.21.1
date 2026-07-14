package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.custom.enums.wisdom.GuBrilliance;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The mind (智道) system. 念 regens by 才情 and tops up on sleep; 意/情 never recover.
//  See CLAUDE.md "Wisdom".
public final class MindService {

    private MindService() {}

    //  ---- read ----
    public static MindData get(Player p) {return p.getData(ModAttachments.MIND);}
    public static MindPool pool(Player p, GuWisdomType t) {return get(p).pool(t);}
    public static long current(Player p, GuWisdomType t) {return pool(p, t).current();}
    public static long max(Player p, GuWisdomType t) {return pool(p, t).max();}
    public static GuBrilliance brilliance(Player p) {return get(p).brilliance();}

    //  ---- write ----
    public static void setCurrent(ServerPlayer p, GuWisdomType t, long v) {set(p, t, pool(p, t).withCurrent(v));}
    public static void addCurrent(ServerPlayer p, GuWisdomType t, long d) {setCurrent(p, t, current(p, t) + d);}
    public static void setMax(ServerPlayer p, GuWisdomType t, long v) {set(p, t, pool(p, t).withMax(v));}
    public static void addMax(ServerPlayer p, GuWisdomType t, long d) {setMax(p, t, max(p, t) + d);}
    public static void empty(ServerPlayer p) {store(p, get(p).emptied());}
    private static void set(ServerPlayer p, GuWisdomType t, MindPool v) {store(p, get(p).with(t, v));}
    private static void store(ServerPlayer p, MindData d) {p.setData(ModAttachments.MIND, d);}

    //  ---- 才情 ----  rolled at birth (PlayerDataService.onBirth), never here; these are the command's.
    public static void setBrilliance(ServerPlayer p, GuBrilliance v) {store(p, get(p).withBrilliance(v));}
    public static void shiftBrilliance(ServerPlayer p, int d) {setBrilliance(p, brilliance(p).shift(d));}

    //  Operator tool: a clean full pool at the cap (never over, so never lethal, and no fatigue flag).
    public static void refill(ServerPlayer p, GuWisdomType t) {
        long cap = max(p, t);
        set(p, t, new MindPool(cap, cap, false));
    }

    //  One regen step, once a second. 念 only, and it stops at the cap -- regen may never fill the buffer.
    public static void regenStep(ServerPlayer player) {
        MindPool thoughts = pool(player, GuWisdomType.THOUGHTS);
        if (thoughts.current() >= thoughts.max()) return;

        long grown = thoughts.current() + brilliance(player).getThoughtsPerSecond();
        setCurrent(player, GuWisdomType.THOUGHTS, Math.min(grown, thoughts.max()));
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

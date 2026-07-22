package com.unknown.guzhenren.attachment.service.mind;

import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The mind [脑海] system. Thoughts regen by Brilliance [才情] and top up on sleep; wills and emotions never do.
//   CLAUDE.md "Wisdom".
public final class MindService {

    private MindService() {}

    //  ---- read ----
    public static MindData get(Player p) {return p.getData(ModAttachments.MIND);}
    public static MindPool pool(Player p, WisdomType t) {return get(p).pool(t);}
    public static long current(Player p, WisdomType t) {return pool(p, t).current();}
    public static long max(Player p, WisdomType t) {return pool(p, t).max();}
    public static Brilliance brilliance(Player p) {return get(p).brilliance();}

    //  ---- write ----
    public static void setCurrent(ServerPlayer p, WisdomType t, long v) {set(p, t, pool(p, t).withCurrent(v));}
    public static void addCurrent(ServerPlayer p, WisdomType t, long d) {setCurrent(p, t, current(p, t) + d);}
    public static void setMax(ServerPlayer p, WisdomType t, long v) {set(p, t, pool(p, t).withMax(v));}
    public static void addMax(ServerPlayer p, WisdomType t, long d) {setMax(p, t, max(p, t) + d);}
    public static void empty(ServerPlayer p) {store(p, get(p).emptied());}
    private static void set(ServerPlayer p, WisdomType t, MindPool v) {store(p, get(p).with(t, v));}
    private static void store(ServerPlayer p, MindData d) {p.setData(ModAttachments.MIND, d);}

    //  ---- Brilliance [才情] ----  rolled at birth (PlayerDataService.onBirth), never here; these are
    //  the command's.
    public static void setBrilliance(ServerPlayer p, Brilliance v) {store(p, get(p).withBrilliance(v));}
    public static void shiftBrilliance(ServerPlayer p, int d) {setBrilliance(p, brilliance(p).shift(d));}

    //  Operator tool: a clean full pool at the cap (never over, so never lethal, and no fatigue flag).
    public static void refill(ServerPlayer p, WisdomType t) {
        long cap = max(p, t);
        set(p, t, new MindPool(cap, cap, false));
    }

    //  One regen step, once a second. Thoughts only, and it stops at the cap -- regen never fills the buffer.
    public static void regenStep(ServerPlayer player) {
        MindPool thoughts = pool(player, WisdomType.THOUGHTS);
        if (thoughts.current() >= thoughts.max()) return;

        long grown = thoughts.current() + brilliance(player).getThoughtsPerSecond();
        setCurrent(player, WisdomType.THOUGHTS, Math.min(grown, thoughts.max()));
    }

    //  A completed sleep restores thoughts only -- wills and emotions do not recover. See MindPool.slept.
    public static void onSleepComplete(ServerPlayer p) {
        set(p, WisdomType.THOUGHTS, pool(p, WisdomType.THOUGHTS).slept());
    }

    //  Spend, all or nothing.
    public static boolean consume(ServerPlayer player, WisdomType type, long amount) {
        if (amount <= 0L) return true;
        long current = current(player, type);
        if (current < amount) return false;
        setCurrent(player, type, current - amount);
        return true;
    }
}

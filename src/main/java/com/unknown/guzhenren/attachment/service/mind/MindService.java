package com.unknown.guzhenren.attachment.service.mind;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * The only writer of Mind [脑海] pools, and the door where every clamp lives.
 *
 * <p>⚠ The clamp cannot live in the record: only some wisdom types may burst past their cap, and a
 * pool does not know which type it belongs to. So every write has to come through here.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class MindService {

    private MindService() {}

    public static final int ZOMBIE_THOUGHT_INTERVAL_TICKS = 5 * Ticks.SECOND;

    private static boolean thinksThisStep(ServerPlayer p) {
        return !BodyService.isZombie(p) || p.tickCount % ZOMBIE_THOUGHT_INTERVAL_TICKS == 0;
    }

    public static MindData get(Player p) {return p.getData(ModAttachments.MIND);}
    public static MindPool pool(Player p, WisdomType t) {return get(p).pool(t);}
    public static long current(Player p, WisdomType t) {return pool(p, t).current();}
    public static long max(Player p, WisdomType t) {return pool(p, t).max();}
    public static Brilliance brilliance(Player p) {return get(p).brilliance();}

    public static void setCurrent(ServerPlayer p, WisdomType t, long v) {
        MindPool pool = pool(p, t);
        set(p, t, pool.withCurrent(t.isBurstable() ? v : Math.min(v, pool.max())));
    }
    public static void addCurrent(ServerPlayer p, WisdomType t, long d) {setCurrent(p, t, current(p, t) + d);}
    public static void setMax(ServerPlayer p, WisdomType t, long v) {set(p, t, pool(p, t).withMax(v));}
    public static void addMax(ServerPlayer p, WisdomType t, long d) {setMax(p, t, max(p, t) + d);}
    public static void empty(ServerPlayer p) {store(p, get(p).emptied());}
    private static void set(ServerPlayer p, WisdomType t, MindPool v) {store(p, get(p).with(t, v));}
    private static void store(ServerPlayer p, MindData d) {p.setData(ModAttachments.MIND, d);}

    public static void setBrilliance(ServerPlayer p, Brilliance v) {store(p, get(p).withBrilliance(v));}
    public static void shiftBrilliance(ServerPlayer p, int d) {setBrilliance(p, brilliance(p).shift(d));}

    public static void refill(ServerPlayer p, WisdomType t) {
        long cap = max(p, t);
        set(p, t, new MindPool(cap, cap, false));
    }

    public static void regenStep(ServerPlayer player) {
        if (!thinksThisStep(player)) return;

        MindPool thoughts = pool(player, WisdomType.THOUGHTS);
        if (thoughts.current() >= thoughts.max()) return;

        long grown = thoughts.current()
                + TimeFlowService.perStep(player, brilliance(player).getThoughtsPerSecond());
        setCurrent(player, WisdomType.THOUGHTS, Math.min(grown, thoughts.max()));
    }

    public static void onSleepComplete(ServerPlayer p) {
        set(p, WisdomType.THOUGHTS, pool(p, WisdomType.THOUGHTS).slept());
    }

    public static boolean consume(ServerPlayer player, WisdomType type, long amount) {
        if (amount <= 0L) return true;
        long current = current(player, type);
        if (current < amount) return false;
        setCurrent(player, type, current - amount);
        return true;
    }
}

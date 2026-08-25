package com.unknown.guzhenren.attachment.service.mind;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The only writer of Mind [脑海] pools, and the door where every clamp lives.
 *
 * <p>Static service over the {@code mind_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. {@code setCurrent} is the single clamp -- it reads {@link WisdomType#isBurstable}
 * to decide whether the value may exceed the cap, so a non-burstable type is hard-capped here. {@code
 * regenStep} is the heartbeat entry; a zombie thinks only every 5th second ({@code ZOMBIE_THOUGHT_INTERVAL_TICKS}).
 *
 * <p>⚠ The clamp cannot live in the record: only some wisdom types may burst past their cap, and a
 * pool does not know which type it belongs to. So every write has to come through here. ⚠ Regan always
 * stops at the cap -- the body must never idle itself into 脑海炸裂 [mind ocean shattered]; only a Gu,
 * item or command may overfill. ⚠ {@code addThoughts} writes the tag total AFTER the current, so a
 * write that lowers current thoughts has the tag totals rescaled by the ctor -- call {@code setCurrent}
 * first or the tag map oversums. ⚠ Sleep restores only HALF the deficit when the buffer was used.
 *
 * @author Alex
 * @version 1.0.0
 * @see MindData
 * @see MindPool
 * @since 1.0.0
 */

public final class MindService {

    private MindService() {}

    public static final int ZOMBIE_THOUGHT_INTERVAL_TICKS = 5 * Ticks.SECOND;

    private static boolean thinksThisStep(ServerPlayer p) {
        return !BodyService.isZombie(p) || p.tickCount % ZOMBIE_THOUGHT_INTERVAL_TICKS == 0;
    }

    public static @NotNull MindData get(@NotNull Player p) {return p.getData(ModAttachments.MIND);}
    public static @NotNull MindPool pool(@NotNull Player p, @NotNull WisdomType t) {return get(p).pool(t);}
    public static long current(@NotNull Player p, @NotNull WisdomType t) {return pool(p, t).current();}
    public static long max(@NotNull Player p, @NotNull WisdomType t) {return pool(p, t).max();}
    public static @NotNull Brilliance brilliance(@NotNull Player p) {return get(p).brilliance();}
    public static @NotNull Map<ThoughtTag, Long> taggedThoughts(@NotNull Player p) {return get(p).taggedThoughts();}
    public static long taggedAmount(@NotNull Player p, @NotNull ThoughtTag tag) {return get(p).taggedThoughts().getOrDefault(tag, 0L);}
    public static long naturalThoughts(@NotNull Player p) {
        long tagged = get(p).taggedThoughts().values().stream().mapToLong(Long::longValue).sum();
        return Math.max(0L, current(p, WisdomType.THOUGHTS) - tagged);
    }

    public static void setCurrent(@NotNull ServerPlayer p, @NotNull WisdomType t, long v) {
        MindPool pool = pool(p, t);
        set(p, t, pool.withCurrent(t.isBurstable() ? v : Math.min(v, pool.max())));
    }
    public static void addCurrent(@NotNull ServerPlayer p, @NotNull WisdomType t, long d) {
        setCurrent(p, t, current(p, t) + d);
    }
    public static void addThoughts(@NotNull ServerPlayer p, long amount, @NotNull ThoughtTag tag) {
        if (amount <= 0L) return;
        setCurrent(p, WisdomType.THOUGHTS, current(p, WisdomType.THOUGHTS) + amount);
        if (tag != ThoughtTag.NATURAL) {
            store(p, get(p).withTagged(tag, taggedAmount(p, tag) + amount));
        }
    }
    public static void setMax(@NotNull ServerPlayer p, @NotNull WisdomType t, long v) {set(p, t, pool(p, t).withMax(v));}
    public static void addMax(@NotNull ServerPlayer p, @NotNull WisdomType t, long d) {setMax(p, t, max(p, t) + d);}
    public static void empty(@NotNull ServerPlayer p) {store(p, get(p).emptied());}
    private static void set(ServerPlayer p, WisdomType t, MindPool v) {store(p, get(p).with(t, v));}
    private static void store(ServerPlayer p, MindData d) {p.setData(ModAttachments.MIND, d);}

    public static void setBrilliance(@NotNull ServerPlayer p, @NotNull Brilliance v) {
        store(p, get(p).withBrilliance(v));
    }
    public static void shiftBrilliance(@NotNull ServerPlayer p, int d) {setBrilliance(p, brilliance(p).shift(d));}

    public static void refill(@NotNull ServerPlayer p, @NotNull WisdomType t) {
        long cap = max(p, t);
        set(p, t, new MindPool(cap, cap, false));
    }

    public static void regenStep(@NotNull ServerPlayer player) {
        if (!thinksThisStep(player)) return;

        MindPool thoughts = pool(player, WisdomType.THOUGHTS);
        if (thoughts.current() >= thoughts.max()) return;

        long grown = thoughts.current()
                + TimeFlowService.perStep(player, brilliance(player).getThoughtsPerSecond());
        setCurrent(player, WisdomType.THOUGHTS, Math.min(grown, thoughts.max()));
    }

    public static void onSleepComplete(@NotNull ServerPlayer p) {
        set(p, WisdomType.THOUGHTS, pool(p, WisdomType.THOUGHTS).slept());
    }

    public static boolean consume(@NotNull ServerPlayer player, @NotNull WisdomType type, long amount) {
        if (amount <= 0L) return true;
        long current = current(player, type);
        if (current < amount) return false;
        setCurrent(player, type, current - amount);
        return true;
    }
}

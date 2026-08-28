package com.unknown.guzhenren.attachment.service.path;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.path.PathQiData;
import com.unknown.guzhenren.attachment.data.path.PathQiEntry;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The only writer of Qi [气] holdings, and where their MobEffects are rebuilt from the pool.
 *
 * <p>Static service over the {@code qi_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Every {@code store} also runs {@code syncEffects}, which rebuilds the four
 * qi MobEffects from the pool so they match what the player is actually holding.
 *
 * <p>⚠ Those effects are a PROJECTION, never the truth -- because the heartbeat rebuilds them, milk
 * and {@code /effect clear} cannot cure Death Qi [死气]; the next tick puts it straight back. ⚠
 * {@code set} re-anchors the hold on the SUM when adding to an existing kind, never "takes the higher
 * grade" and never refuses -- death qi accumulates like every other kind. ⚠ The graded syncs
 * ({@code syncGraded}) compute the tier off the CURRENT amount, so a kind past its hold that has
 * decayed down a tier bracket reads a lower-tier effect -- his short-term {@code TODO(decay)} is that
 * a kind past its hold reads NO effect while decaying. ⚠ {@code syncDeath} refreshes with a 20-tick
 * duration so a missed heartbeat does not let the curse lapse.
 *
 * @author Alex
 * @version 1.0.0
 * @see PathQiData
 * @see PathQiEntry
 * @since 1.0.0
 */

public final class PathQiService {

    private PathQiService() {}

    private static final int EFFECT_REFRESH_TICKS = 2 * Ticks.SECOND;

    public static @NotNull PathQiData get(@NotNull Player p) {return p.getData(ModAttachments.QI);}
    public static long current(@NotNull Player p, @NotNull QiKind kind) {return get(p).current(kind, now(p));}

    public static void add(@NotNull ServerPlayer p, @NotNull QiKind kind, long delta) {
        set(p, kind, current(p, kind) + delta);
    }

    public static void set(@NotNull ServerPlayer p, @NotNull QiKind kind, long value) {
        long amount = Math.max(0L, value);
        long holdEnd = 0L;
        if (kind.isTimed() && amount > 0L) {
            holdEnd = now(p) + kind.holdTicks(Math.max(0, QiKind.tierOf(amount)));
        }
        store(p, get(p).with(kind, new PathQiEntry(amount, holdEnd)));
    }

    @SuppressWarnings("resource")
    private static long now(Player p) {return p.level().getGameTime();}

    private static void store(ServerPlayer p, PathQiData data) {
        long now = now(p);
        PathQiData pruned = data;
        for (QiKind kind : QiKind.values()) {
            if (data.get(kind) != null && data.current(kind, now) <= 0L) pruned = pruned.without(kind);
        }
        if (pruned.equals(get(p))) return;
        p.setData(ModAttachments.QI, pruned);
        syncEffects(p);
    }

    //region effect projection -- the store is the truth, the MobEffect is its display
    //    TODO(decay): a kind past its hold reads NO effect while decaying -- per-tier falloff is his short-term TODO.
    public static void syncEffects(@NotNull ServerPlayer player) {
        long now = now(player);
        syncGraded(player, QiKind.STRENGTH, ModEffects.STRENGTH_QI, now);
        syncGraded(player, QiKind.LIFE, ModEffects.LIFE_QI, now);
        syncGraded(player, QiKind.ESSENCE, ModEffects.ESSENCE_QI, now);
        syncDeath(player, now);
    }

    private static void syncGraded(ServerPlayer player, QiKind kind, Holder<MobEffect> effect, long now) {
        PathQiData data = get(player);
        int tier = data.holding(kind, now) ? QiKind.tierOf(data.current(kind, now)) : -1;
        MobEffectInstance current = player.getEffect(effect);

        if (tier < 0) {
            if (current != null) player.removeEffect(effect);
            return;
        }

        PathQiEntry entry = data.get(kind);
        int duration = entry == null ? EFFECT_REFRESH_TICKS
                : (int) Math.min(Integer.MAX_VALUE, entry.holdEndTick() - now + EFFECT_REFRESH_TICKS);
        if (current == null || current.getAmplifier() != tier
                || current.getDuration() < EFFECT_REFRESH_TICKS) {
            player.addEffect(ModEffects.instance(effect, duration, tier));
        }
    }

    private static void syncDeath(ServerPlayer player, long now) {
        boolean present = get(player).current(QiKind.DEATH, now) > 0L;
        MobEffectInstance current = player.getEffect(ModEffects.DEATH_QI);
        if (!present) {
            if (current != null) player.removeEffect(ModEffects.DEATH_QI);
            return;
        }
        if (current == null || current.getDuration() < EFFECT_REFRESH_TICKS) {
            player.addEffect(ModEffects.instance(ModEffects.DEATH_QI, 10 * EFFECT_REFRESH_TICKS, 0));
        }
    }
    //endregion
}

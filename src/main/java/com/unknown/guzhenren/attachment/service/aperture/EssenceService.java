package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.effect.pool.EssenceQiEffect;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModEffects;
import java.util.Arrays;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Essence [真元]: the pool, the distilled reserve, its regen, and the Liquor Worm's [酒虫] three phases.
 *
 * <p>Static service; reads take {@link Player}, writes take {@link ServerPlayer} and route through
 * {@link ApertureService#set} (which fires {@link
 * com.unknown.guzhenren.attachment.service.body.HealthService#refresh}). {@code regenStep} is the
 * heartbeat entry point; it carries a float remainder in {@code ESSENCE_CARRY} (unsynced, unserialized,
 * mutated in place) so a fractional regen banks across ticks instead of being lost.
 *
 * <p>⚠ A gate must ask {@code spendable()}, never {@code currentEssence()} -- distilling empties the
 * ordinary pool by design, so a gate on the raw value refuses everything for that whole phase. ⚠
 * {@code consume} burns the distilled reserve at the 1:2 rate FIRST, then the ordinary pool; the
 * distilled half is rounded UP so the last point cannot pay for itself twice. ⚠ Every path that SKIPS
 * a regen step (death-qi choke, zombie) must zero the carry -- both do today; the two {@code = 0.0F}
 * writes are the mechanic, not tidying. ⚠ {@code isChoked} outranks the liquor redirect AND the
 * essence-qi bonus -- it is checked first in {@code regenStep} and returns.
 *
 * @author Alex
 * @version 1.0.0
 * @see ApertureService
 * @see TimeFlowService
 * @since 1.0.0
 */

public final class EssenceService {

    private EssenceService() {}

    public static final long BASE_REGEN_PER_DAY = 100L;
    public static final int REGEN_INTERVAL_TICKS = Ticks.SECOND;
    public static final double HALF_ZOMBIE_REGEN_RATE = 0.5;

    public static long regenPerDay(@NotNull Aperture a) {
        return BASE_REGEN_PER_DAY * a.talent().getRegenRate() * a.rank().getRankBase()
                * a.stage().getEssenceMultiplier();
    }

    public static double regenPerTick(@NotNull Aperture a) {return regenPerDay(a) / (double) Ticks.DAY;}

    public static final long DISTILLED_RATE = 2L;

    public static long currentEssence(@NotNull Player p) {return ApertureService.aperture(p).currentEssence();}
    public static long maxEssence(@NotNull Player p) {return ApertureService.aperture(p).maxEssence();}
    public static long distilledEssence(@NotNull Player p) {return ApertureService.aperture(p).distilledEssence();}

    public static long spendable(@NotNull Player p) {
        return currentEssence(p) + distilledEssence(p) * DISTILLED_RATE;
    }

    public static boolean isDistilling(@NotNull Player p) {return p.hasEffect(ModEffects.LIQUOR_WORM);}

    public static boolean isChoked(@NotNull Player p) {return p.hasEffect(ModEffects.DEATH_QI);}

    public static double essenceQiBonus(@NotNull Player player) {
        MobEffectInstance effect = player.getEffect(ModEffects.ESSENCE_QI);
        return effect == null ? 0.0 : EssenceQiEffect.bonus(effect.getAmplifier());
    }

    public static void add(@NotNull ServerPlayer p, long d) {set(p, currentEssence(p) + d);}
    public static void set(@NotNull ServerPlayer p, long v) {set(p, ApertureService.PRIMARY, v);}

    public static void set(@NotNull ServerPlayer player, int index, long value) {
        ApertureService.set(player, index, ApertureService.aperture(player, index).withCurrentEssence(value));
    }

    public static void refill(@NotNull ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            ApertureService.set(player, i, data.get(i).refilled());
        }
    }

    public static void addDistilled(@NotNull ServerPlayer p, long d) {setDistilled(p, distilledEssence(p) + d);}
    public static void setDistilled(@NotNull ServerPlayer p, long v) {setDistilled(p, ApertureService.PRIMARY, v);}

    public static void setDistilled(@NotNull ServerPlayer player, int index, long value) {
        ApertureService.set(player, index,
                ApertureService.aperture(player, index).withDistilledEssence(value));
    }

    //region the three phases of a Liquor Worm [酒虫]
    public static void beginDistilling(@NotNull ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            ApertureService.set(player, i, data.get(i).withCurrentEssence(0L));
        }
    }

    public static void endDistilling(@NotNull ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            Aperture aperture = data.get(i);
            long left = aperture.distilledEssence();
            if (left <= 0L) continue;

            ApertureService.set(player, i, aperture
                    .withCurrentEssence(aperture.currentEssence() + left * DISTILLED_RATE)
                    .withDistilledEssence(0L));
        }
    }
    //endregion

    public static boolean consume(@NotNull ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        if (spendable(player) < amount) return false;

        long distilled = distilledEssence(player);
        long fromDistilled = Math.min(distilled, (amount + DISTILLED_RATE - 1) / DISTILLED_RATE);
        long covered = fromDistilled * DISTILLED_RATE;

        if (fromDistilled > 0L) setDistilled(player, ApertureService.PRIMARY, distilled - fromDistilled);
        long remainder = Math.max(0L, amount - covered);
        if (remainder > 0L) set(player, currentEssence(player) - remainder);
        return true;
    }

    public static void regenStep(@NotNull ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        float[] carry = player.getData(ModAttachments.ESSENCE_CARRY);

        if (isChoked(player) || BodyService.isZombie(player)) {
            Arrays.fill(carry, 0.0F);
            return;
        }

        boolean distilling = isDistilling(player);
        double bonus = essenceQiBonus(player);
        double halfZombieRate = BodyService.isHalfZombie(player) ? HALF_ZOMBIE_REGEN_RATE : 1.0;

        for (int i = 0; i < data.count(); i++) {
            Aperture aperture = data.get(i);
            long current = distilling ? aperture.distilledEssence() : aperture.currentEssence();

            if (current >= aperture.maxEssence()) {
                carry[i] = 0.0F;
                continue;
            }

            double perStep = TimeFlowService.perStep(player,
                    regenPerTick(aperture) * REGEN_INTERVAL_TICKS * (1.0 + bonus) * halfZombieRate);
            if (perStep <= 0.0) continue;

            double total = carry[i] + perStep;
            long whole = (long) total;

            carry[i] = (float) (total - whole);
            if (whole <= 0L) continue;

            if (distilling) {
                setDistilled(player, i, current + whole);
            } else {
                set(player, i, current + whole);
            }
        }
    }
}

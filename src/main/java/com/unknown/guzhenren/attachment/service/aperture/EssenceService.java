package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The essence [真元] system: one pool per aperture. Formulas and worked examples: CLAUDE.md "Formulas".
//  ⚠ Every write here is a silent no-op on an unawakened player -- the caller must refuse first.
//  The cap is Aperture's own derived value and its ctor clamps against it -- nothing here has to.
public final class EssenceService {

    private EssenceService() {}

    //  A *notional* aptitude base -- regen is measured against 100, never the player's own base.
    public static final long BASE_REGEN_PER_DAY = 100L;

    //  Once a second, not once a tick: same result thanks to ESSENCE_CARRY, twenty times fewer writes.
    public static final int REGEN_INTERVAL_TICKS = 20;

    //  ---- derived, pure ----
    //  ⚠ A dead aperture draws in no ambient qi. The gate is the aperture's own state, not the body's.
    public static long regenPerDay(Aperture a) {
        if (!a.isAlive()) return 0L;
        return BASE_REGEN_PER_DAY * a.talent().getRegenRate() * a.rank().getRankBase()
                * a.stage().getEssenceMultiplier();
    }

    public static double regenPerTick(Aperture a) {return regenPerDay(a) / (double) BodyService.TICKS_PER_DAY;}

    //  ⚠ One distilled point pays for two ordinary ones. The single place that ratio is written down.
    public static final long DISTILLED_RATE = 2L;

    //  ---- read ----  no index means the primary aperture, the only one a command can reach today.
    public static long currentEssence(Player p) {return ApertureService.aperture(p).currentEssence();}
    public static long maxEssence(Player p) {return ApertureService.aperture(p).maxEssence();}
    public static long distilledEssence(Player p) {return ApertureService.aperture(p).distilledEssence();}

    //  What he can actually pay with -- distilled counts double.  ⚠ Gates must read THIS, not
    //  currentEssence: phase 1 empties the ordinary pool, so a gate on it refuses mid-effect.
    public static long spendable(Player p) {
        return currentEssence(p) + distilledEssence(p) * DISTILLED_RATE;
    }

    public static boolean isDistilling(Player p) {return p.hasEffect(ModEffects.LIQUOR_WORM);}

    //  ---- write ----
    public static void add(ServerPlayer p, long d) {set(p, currentEssence(p) + d);}
    public static void set(ServerPlayer p, long v) {set(p, ApertureService.PRIMARY, v);}

    public static void set(ServerPlayer player, int index, long value) {
        ApertureService.set(player, index, ApertureService.aperture(player, index).withCurrentEssence(value));
    }

    //  Every aperture a player owns -- a completed sleep fills them all.
    public static void refill(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            ApertureService.set(player, i, data.get(i).refilled());
        }
    }

    public static void addDistilled(ServerPlayer p, long d) {setDistilled(p, distilledEssence(p) + d);}
    public static void setDistilled(ServerPlayer p, long v) {setDistilled(p, ApertureService.PRIMARY, v);}

    public static void setDistilled(ServerPlayer player, int index, long value) {
        ApertureService.set(player, index,
                ApertureService.aperture(player, index).withDistilledEssence(value));
    }

    //region the three phases of a Liquor Worm [酒虫]
    //  Phase 1 -- every aperture's ordinary pool goes to zero at once. Regen does not stop by a flag:
    //  regenStep simply redirects while the effect runs, which is the same fact with nothing to desync.
    public static void beginDistilling(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            ApertureService.set(player, i, data.get(i).withCurrentEssence(0L));
        }
    }

    //  Phase 3's close -- what he never got round to spending pays back at the same 1:2, then the pool
    //  empties. ⚠ Aperture's ctor clamps the result, so a big leftover is truncated at the cap, not lost
    //  quietly somewhere else. Called from PlayerTickEvents, never from the effect: there is no hook.
    public static void endDistilling(ServerPlayer player) {
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

    //  Spend, all or nothing. The primary aperture pays, distilled first because it is worth double.
    //  ⚠ All-or-nothing is measured against spendable(), not currentEssence -- otherwise a distilling
    //  cultivator, whose ordinary pool is 0 by design, could never pay for anything.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        if (spendable(player) < amount) return false;

        long distilled = distilledEssence(player);
        //  Round UP: half a distilled point cannot be spent, so an odd cost takes the whole one.
        long fromDistilled = Math.min(distilled, (amount + DISTILLED_RATE - 1) / DISTILLED_RATE);
        long covered = fromDistilled * DISTILLED_RATE;

        if (fromDistilled > 0L) setDistilled(player, ApertureService.PRIMARY, distilled - fromDistilled);
        //  ⚠ covered may EXCEED amount by one, on an odd cost. The remainder floors at zero rather than
        //  refunding, or the last distilled point would pay for itself twice.
        long remainder = Math.max(0L, amount - covered);
        if (remainder > 0L) set(player, currentEssence(player) - remainder);
        return true;
    }

    //  One step per aperture, every REGEN_INTERVAL_TICKS. The carry is indexed by aperture.
    //  ⚠ Phase 2: while a Liquor Worm runs, the SAME rate fills the distilled pool instead ("自然恢复效率
    //  不变"). That redirect is also what makes phase 1's "regen stops" true, with no second flag.  CLAUDE.md.
    public static void regenStep(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        float[] carry = player.getData(ModAttachments.ESSENCE_CARRY);
        boolean distilling = isDistilling(player);

        for (int i = 0; i < data.count(); i++) {
            Aperture aperture = data.get(i);
            long current = distilling ? aperture.distilledEssence() : aperture.currentEssence();

            if (current >= aperture.maxEssence()) {
                //  At the cap: drop the carry, or it banks up and dumps the instant the cap rises.
                carry[i] = 0.0F;
                continue;
            }

            double perStep = regenPerTick(aperture) * REGEN_INTERVAL_TICKS;
            if (perStep <= 0.0) continue;

            double total = carry[i] + perStep;
            long whole = (long) total;

            //  Unsynced and unserialized, so this write is free -- no packet.  CLAUDE.md "Networking".
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

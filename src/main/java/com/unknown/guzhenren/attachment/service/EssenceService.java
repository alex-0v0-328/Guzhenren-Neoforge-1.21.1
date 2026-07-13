package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The essence (真元) system.
//
//  The cap and the regen rate are both pure functions of CoreData, so they are static functions the
//  client can call too -- the HUD derives the cap from the synced CoreData rather than being told it.
public final class EssenceService {

    private EssenceService() {}

    public static final int TICKS_PER_DAY = 24000;

    //  Regen is measured against a notional baseEssence of 100, not the player's own, so that a
    //  丁等 cultivator refills in the same number of days as a 甲等 one -- aptitude buys you a
    //  bigger pool, not a slower one.
    public static final long BASE_REGEN_PER_DAY = 100L;

    //  Regen is stepped once a second, not once a tick: identical result thanks to ESSENCE_CARRY,
    //  twenty times fewer writes.
    public static final int REGEN_INTERVAL_TICKS = 20;

    //  ---- derived, pure ----

    //  KNOWN HOLE: GuRank.SIX..NINE have rankBase == 0, so every immortal-rank player caps at 0.
    //  That is intentional for now -- immortals are a placeholder and will not use the essence
    //  system at all. When they get their own system, this is the one place that has to change.
    public static long maxEssence(CoreData core) {
        long max = (long) core.baseEssence() * core.stage().getEssenceMultiplier() * core.rank().getRankBase();
        return Math.max(0L, max);
    }

    public static long maxEssence(Player p) {return maxEssence(CoreService.get(p));}
    public static double regenPerTick(CoreData core) {return regenPerDay(core) / (double) TICKS_PER_DAY;}

    //  A zombified cultivator's aperture is dead and cannot draw in ambient qi -- see GuLifeState.
    public static long regenPerDay(CoreData core) {
        if (core.lifeState() != GuLifeState.ALIVE) return 0L;
        return BASE_REGEN_PER_DAY
                * core.talent().getRegenRate()
                * core.rank().getRankBase()
                * core.stage().getEssenceMultiplier();
    }

    //  ---- read ----
    public static EssenceData get(Player p) {return p.getData(ModAttachments.ESSENCE);}
    public static long current(Player p) {return get(p).current();}

    //  ---- write ----
    public static void add(ServerPlayer p, long delta) {set(p, current(p) + delta);}
    public static void refill(ServerPlayer p) {set(p, maxEssence(p));}

    //  Called by CoreService whenever core changes: dropping a rank lowers the cap, and a player
    //  must not be left holding more essence than they can now contain.
    public static void clampToMax(ServerPlayer p) {set(p, current(p));}

    //  setData is what syncs; see ModAttachments.
    public static void set(ServerPlayer player, long value) {
        long clamped = Math.clamp(value, 0L, maxEssence(player));
        if (clamped == current(player)) return;
        player.setData(ModAttachments.ESSENCE, new EssenceData(clamped));
    }

    //  Spend essence, all or nothing. Returns false and changes nothing if the pool is short.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        long current = current(player);
        if (current < amount) return false;
        set(player, current - amount);
        return true;
    }

    //  One regen step. Called from PlayerTickEvents every REGEN_INTERVAL_TICKS.
    public static void regenStep(ServerPlayer player) {
        CoreData core = CoreService.get(player);
        long max = maxEssence(core);
        long current = current(player);

        if (current >= max) {
            //  Sitting at the cap: drop the carry so it cannot quietly bank up and then dump into
            //  the pool the instant a breakthrough raises the cap.
            if (player.getData(ModAttachments.ESSENCE_CARRY) != 0.0F) {
                player.setData(ModAttachments.ESSENCE_CARRY, 0.0F);
            }
            return;
        }

        double perStep = regenPerTick(core) * REGEN_INTERVAL_TICKS;
        if (perStep <= 0.0) return;

        double total = player.getData(ModAttachments.ESSENCE_CARRY) + perStep;
        long whole = (long) total;

        //  Unsynced attachment: this write is free, no packet.
        player.setData(ModAttachments.ESSENCE_CARRY, (float) (total - whole));

        if (whole > 0L) {
            set(player, current + whole);
        }
    }
}

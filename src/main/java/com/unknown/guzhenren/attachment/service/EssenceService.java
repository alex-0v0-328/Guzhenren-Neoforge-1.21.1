package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.EssenceData;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The essence (真元) system. Formulas and worked examples: CLAUDE.md "Formulas".
//  Cap and regen rate are pure functions of CoreData, so the HUD calls them on the client too.
public final class EssenceService {

    private EssenceService() {}

    public static final int TICKS_PER_DAY = 24000;

    //  A *notional* aptitude base -- regen is measured against 100, never the player's own base.
    public static final long BASE_REGEN_PER_DAY = 100L;

    //  Once a second, not once a tick: same result thanks to ESSENCE_CARRY, twenty times fewer writes.
    public static final int REGEN_INTERVAL_TICKS = 20;

    //  ---- derived, pure ----

    //  ⚠ GuRank.SIX..NINE have rankBase == 0, so an immortal caps at 0. Deliberate -- do not "fix" it.
    public static long maxEssence(CoreData core) {
        long max = (long) core.baseEssence() * core.stage().getEssenceMultiplier() * core.rank().getRankBase();
        return Math.max(0L, max);
    }

    public static long maxEssence(Player p) {return maxEssence(CoreService.get(p));}
    public static double regenPerTick(CoreData core) {return regenPerDay(core) / (double) TICKS_PER_DAY;}

    //  A zombified aperture is dead and cannot draw in ambient qi -- see GuLifeState.
    public static long regenPerDay(CoreData core) {
        if (core.lifeState() != GuLifeState.ALIVE) return 0L;
        return BASE_REGEN_PER_DAY
                * core.talent().getRegenRate()
                * core.rank().getRankBase()
                * core.stage().getEssenceMultiplier();
    }

    //  ---- read ----
    public static EssenceData get(Player p) {return p.getData(ModAttachments.ESSENCE);}
    public static long currentEssence(Player p) {return get(p).currentEssence();}

    //  ---- write ----
    public static void add(ServerPlayer p, long delta) {set(p, currentEssence(p) + delta);}
    public static void refill(ServerPlayer p) {set(p, maxEssence(p));}

    //  Called by CoreService on every core change: dropping a rank lowers the cap.
    public static void clampToMax(ServerPlayer p) {set(p, currentEssence(p));}

    //  setData is what syncs; see ModAttachments.
    public static void set(ServerPlayer player, long value) {
        long clamped = Math.clamp(value, 0L, maxEssence(player));
        if (clamped == currentEssence(player)) return;
        player.setData(ModAttachments.ESSENCE, new EssenceData(clamped));
    }

    //  Spend, all or nothing.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        long current = currentEssence(player);
        if (current < amount) return false;
        set(player, current - amount);
        return true;
    }

    //  One regen step, every REGEN_INTERVAL_TICKS.
    public static void regenStep(ServerPlayer player) {
        CoreData core = CoreService.get(player);
        long max = maxEssence(core);
        long current = currentEssence(player);

        if (current >= max) {
            //  At the cap: drop the carry, or it banks up and dumps in the instant the cap rises.
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

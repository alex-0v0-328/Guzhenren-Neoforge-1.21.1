package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The essence (真元) system: one pool per aperture. Formulas and worked examples: CLAUDE.md "Formulas".
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

    //  ---- read ----  no index means the primary aperture, the only one a command can reach today.
    public static long currentEssence(Player p) {return ApertureService.aperture(p).currentEssence();}
    public static long maxEssence(Player p) {return ApertureService.aperture(p).maxEssence();}

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

    //  Spend, all or nothing. The primary aperture pays.
    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L) return true;
        long current = currentEssence(player);
        if (current < amount) return false;
        set(player, current - amount);
        return true;
    }

    //  One step per aperture, every REGEN_INTERVAL_TICKS. The carry is indexed by aperture.
    public static void regenStep(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        float[] carry = player.getData(ModAttachments.ESSENCE_CARRY);

        for (int i = 0; i < data.count(); i++) {
            Aperture aperture = data.get(i);
            long current = aperture.currentEssence();

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
            if (whole > 0L) set(player, i, current + whole);
        }
    }
}

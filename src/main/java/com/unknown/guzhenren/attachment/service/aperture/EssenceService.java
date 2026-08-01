package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.effect.EssenceQiEffect;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModEffects;
import java.util.Arrays;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public final class EssenceService {

    private EssenceService() {}

    public static final long BASE_REGEN_PER_DAY = 100L;
    public static final int REGEN_INTERVAL_TICKS = Ticks.SECOND;

    public static long regenPerDay(Aperture a) {
        if (!a.isAlive()) return 0L;
        return BASE_REGEN_PER_DAY * a.talent().getRegenRate() * a.rank().getRankBase()
                * a.stage().getEssenceMultiplier();
    }

    public static double regenPerTick(Aperture a) {return regenPerDay(a) / (double) Ticks.DAY;}

    public static final long DISTILLED_RATE = 2L;

    public static long currentEssence(Player p) {return ApertureService.aperture(p).currentEssence();}
    public static long maxEssence(Player p) {return ApertureService.aperture(p).maxEssence();}
    public static long distilledEssence(Player p) {return ApertureService.aperture(p).distilledEssence();}

    public static long spendable(Player p) {
        return currentEssence(p) + distilledEssence(p) * DISTILLED_RATE;
    }

    public static boolean isDistilling(Player p) {return p.hasEffect(ModEffects.LIQUOR_WORM);}

    public static boolean isChoked(Player p) {return p.hasEffect(ModEffects.DEATH_QI);}

    public static double essenceQiBonus(Player player) {
        MobEffectInstance effect = player.getEffect(ModEffects.ESSENCE_QI);
        return effect == null ? 0.0 : EssenceQiEffect.bonus(effect.getAmplifier());
    }

    public static void add(ServerPlayer p, long d) {set(p, currentEssence(p) + d);}
    public static void set(ServerPlayer p, long v) {set(p, ApertureService.PRIMARY, v);}

    public static void set(ServerPlayer player, int index, long value) {
        ApertureService.set(player, index, ApertureService.aperture(player, index).withCurrentEssence(value));
    }

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
    public static void beginDistilling(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        for (int i = 0; i < data.count(); i++) {
            ApertureService.set(player, i, data.get(i).withCurrentEssence(0L));
        }
    }

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

    public static boolean consume(ServerPlayer player, long amount) {
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

    public static void regenStep(ServerPlayer player) {
        ApertureData data = ApertureService.get(player);
        float[] carry = player.getData(ModAttachments.ESSENCE_CARRY);

        if (isChoked(player)) {
            Arrays.fill(carry, 0.0F);
            return;
        }

        boolean distilling = isDistilling(player);
        double bonus = essenceQiBonus(player);

        for (int i = 0; i < data.count(); i++) {
            Aperture aperture = data.get(i);
            long current = distilling ? aperture.distilledEssence() : aperture.currentEssence();

            if (current >= aperture.maxEssence()) {
                carry[i] = 0.0F;
                continue;
            }

            double perStep = regenPerTick(aperture) * REGEN_INTERVAL_TICKS * (1.0 + bonus);
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

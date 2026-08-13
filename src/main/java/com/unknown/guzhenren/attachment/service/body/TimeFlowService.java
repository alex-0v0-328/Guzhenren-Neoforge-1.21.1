package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.effect.TimeFlowContributor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * The only door the player's own clock [自身时间] is hastened through, and 宙道 [Time Path] is all that
 * comes to it.
 *
 * <p>⚠ A Gu's own clock and the world's clock never pass through here. A time anchor rescaled after the
 * fact reports a quantity that was never true, and nothing checks it.
 *
 * @author Alex
 * @since 1.0.0
 * @see AttackService
 */
public final class TimeFlowService {

    private TimeFlowService() {}

    public static final int NORMAL_RATE = 1;

    /** ⚠ Ageing is banked in these, not in ticks: every rate divides six, so no heartbeat loses a part. */
    public static final long PARTS_PER_TICK = 6L;

    public static int rate(Player player) {
        int rate = NORMAL_RATE;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof TimeFlowContributor contributor) {
                rate = Math.max(rate, contributor.timeRate(instance.getAmplifier()));
            }
        }
        //   TODO(宙道造诣): the attainment term joins HERE, so that no caller has to learn about it.
        return rate;
    }

    public static boolean hastened(Player p) {return rate(p) > NORMAL_RATE;}

    //region 自身时间 [his own clock] -- two verbs, because it only ever takes two shapes
    /** A stretch he has to sit through: a press held down, a cooldown, a ritual waited out. */
    public static int waited(Player p, int ticks) {return waited(rate(p), ticks);}

    /** What he earns or spends in one step, so a hastened clock moves MORE per step, never more often. */
    public static long perStep(Player p, long amount) {return perStep(rate(p), amount);}
    public static double perStep(Player p, double amount) {return perStep(rate(p), amount);}

    /** The share of a stretch he lived through without spending it, which is what ageing hands back. */
    public static long skipped(Player p, int ticks) {return skipped(rate(p), ticks);}
    //endregion

    //region the arithmetic alone -- a rate rather than a player, so it can be asserted without a world
    /** ⚠ Floored at one tick: a short wait divided by a fast clock is zero, which reads as "no wait". */
    public static int waited(int rate, int ticks) {return ticks <= 0 ? ticks : Math.max(1, ticks / rate);}

    public static long perStep(int rate, long amount) {return amount * rate;}
    public static double perStep(int rate, double amount) {return amount * rate;}

    public static long skipped(int rate, int ticks) {
        long parts = ticks * PARTS_PER_TICK;
        return parts - parts / rate;
    }
    //endregion
}

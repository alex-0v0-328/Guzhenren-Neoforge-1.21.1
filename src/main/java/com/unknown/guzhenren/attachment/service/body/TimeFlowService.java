package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.effect.TimeFlowContributor;
import net.minecraft.server.level.ServerPlayer;
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

    public static int rate(Player player) {
        int rate = 0;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof TimeFlowContributor contributor) {
                rate += contributor.timeRate(instance.getAmplifier());
            }
        }
        if (rate < NORMAL_RATE) return NORMAL_RATE;
        //   TODO(宙道造诣): a grade term joins HERE, so that no caller has to learn about it.
        return rate;
    }

    public static boolean hastened(Player p) {return rate(p) > NORMAL_RATE;}

    /** What the running forms have booked onto the Time Flow tag, and so owe back the moment they end. */
    public static long specks(Player player) {
        long total = 0L;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof TimeFlowContributor contributor) {
                total += contributor.timeSpecks(instance.getAmplifier());
            }
        }
        return total;
    }

    /**
     * ⚠ The tag is a PROJECTION of the running forms, never a grant and a matching revoke. That is what
     * makes milk, {@code /effect clear} and death take the specks back too, none of which fires a hook.
     */
    public static void syncSpecks(ServerPlayer player) {
        long booked = specks(player);
        if (PathService.speck(player, GuPath.TIME, MarkTag.TIME_FLOW) == booked) return;

        PathService.setSpeck(player, GuPath.TIME, MarkTag.TIME_FLOW, booked);
    }

    //region 自身时间 [his own clock] -- two verbs, because it only ever takes two shapes
    /** A stretch he has to sit through: a press held down, a cooldown, a ritual waited out. */
    public static int waited(Player p, int ticks) {return waited(rate(p), ticks);}

    /** What he earns or spends in one step, so a hastened clock moves MORE per step, never more often. */
    public static long perStep(Player p, long amount) {return perStep(rate(p), amount);}
    public static double perStep(Player p, double amount) {return perStep(rate(p), amount);}

    //endregion

    //region the arithmetic alone -- a rate rather than a player, so it can be asserted without a world
    /** ⚠ Floored at one tick: a short wait divided by a fast clock is zero, which reads as "no wait". */
    public static int waited(int rate, int ticks) {return ticks <= 0 ? ticks : Math.max(1, ticks / rate);}

    public static long perStep(int rate, long amount) {return amount * rate;}
    public static double perStep(int rate, double amount) {return amount * rate;}
    //endregion
}

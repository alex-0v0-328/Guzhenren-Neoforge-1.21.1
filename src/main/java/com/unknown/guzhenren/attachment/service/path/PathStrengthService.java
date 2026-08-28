package com.unknown.guzhenren.attachment.service.path;

import com.unknown.guzhenren.attachment.data.path.PathStrengthData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyAttackService;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthPathBranch;
import com.unknown.guzhenren.registry.attachment.ModAttachments;
import com.unknown.guzhenren.registry.effect.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Strength [力道]: what has been accumulated, and how much of it a body can actually bring to bear.
 *
 * <p>Static service over the {@code strength_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer} and route through {@code store} (which fires {@link BodyAttackService#refresh}).
 * {@code usableJin} is the one reader of the 承受上限 [capacity] ramp; {@code isUnleashed} checks the
 * 全力以赴 effect, which lifts the ramp to the raw total.
 *
 * <p>⚠ {@code usableJin(int, int)} is a deliberate seam so the ramp can be unit-tested without a
 * {@link Player} -- keep it. The ramp had a boundary bug once ({@code min(total, cap+20)} jumped 101
 * straight to 120), and only arithmetic catches that kind. ⚠ The tail is EARNED over
 * {@code [capacity, capacity × LOCK_MULTIPLE]} in 20 linear steps, never granted at once; the lock
 * scales WITH the physique [体质] (cap 300 locks at 3000), so an absolute 1000 would make a better
 * physique's ramp steeper, which is backwards. ⚠ 兽力 sits OUTSIDE the ramp and outside 全力以赴's lift
 * -- it counts in 一猪之力, not 斤. ⚠ A mortal reads {@code Aperture.NONE} → capacity 100, the intended
 * default; a cross-domain READ, not a grant, so it does not count toward the coordinator threshold.
 *
 * @author Alex
 * @version 1.0.0
 * @see PathStrengthData
 * @see BodyAttackService
 * @since 1.0.0
 */

public final class PathStrengthService {

    private PathStrengthService() {}

    public static final int OVERFLOW_JIN = 20;
    public static final int LOCK_MULTIPLE = 10;

    public static @NotNull PathStrengthData get(@NotNull Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(@NotNull Player p, @NotNull BeastStrength b) {return get(p).has(b);}
    public static int humanStrength(@NotNull Player p, @NotNull HumanStrength k) {return get(p).humanStrengthCount(k);}
    public static boolean hasPathBranch(@NotNull Player p, @NotNull StrengthPathBranch b) {
        return get(p).hasPathBranch(b);
    }

    //region what the body can actually bring to bear [承受上限]
    public static int capacity(@NotNull Player p) {
        int base = ApertureService.aperture(p).extremePhysique().getStrengthCapacity();
        if (!p.hasEffect(ModEffects.HARDSHIP_STRENGTH_GU)) return base;

        double healthFraction = (double) p.getHealth() / p.getMaxHealth();
        return base + hardshipCapacityBonus(healthFraction);
    }

    public static int hardshipCapacityBonus(double healthFraction) {
        if (healthFraction > 0.6D) return 0;
        if (healthFraction > 0.5D) return 20;
        if (healthFraction > 0.4D) return 40;
        if (healthFraction > 0.3D) return 60;
        if (healthFraction > 0.2D) return 80;
        if (healthFraction > 0.1D) return 100;
        return 120;
    }

    public static boolean isUnleashed(@NotNull Player p) {return p.hasEffect(ModEffects.ALL_OUT_EFFORT);}

    public static int usableJin(@NotNull Player p) {
        int total = get(p).totalJin();
        return isUnleashed(p) ? total : usableJin(capacity(p), total);
    }

    public static int usableJin(int capacity, int total) {
        if (total <= capacity) return total;

        int span = capacity * (LOCK_MULTIPLE - 1);
        return capacity + OVERFLOW_JIN * Math.min(total - capacity, span) / span;
    }
    //endregion

    public static void grant(@NotNull ServerPlayer p, @NotNull BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(@NotNull ServerPlayer p, @NotNull BeastStrength b) {store(p, get(p).without(b));}
    public static void clear(@NotNull ServerPlayer p) {store(p, PathStrengthData.DEFAULT);}
    private static void store(ServerPlayer p, PathStrengthData d) {
        p.setData(ModAttachments.STRENGTH, d);
        BodyAttackService.refresh(p);
    }

    public static void setHumanStrength(@NotNull ServerPlayer p, @NotNull HumanStrength k, int v) {
        store(p, get(p).withHumanStrength(k, v));
    }
    public static void addHumanStrength(@NotNull ServerPlayer p, @NotNull HumanStrength k, int d) {
        setHumanStrength(p, k, humanStrength(p, k) + d);
    }
}

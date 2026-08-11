package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Strength [力道]: what has been accumulated, and how much of it a body can actually bring to bear.
 *
 * <p>⚠ {@code usableJin(int, int)} is a deliberate seam so the ramp can be unit-tested without a
 * Player. Keep it -- the ramp had a boundary bug once, and only arithmetic catches that kind.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class StrengthService {

    private StrengthService() {}

    public static final int OVERFLOW_JIN = 20;
    public static final int LOCK_MULTIPLE = 10;

    public static StrengthData get(Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(Player p, BeastStrength b) {return get(p).has(b);}
    public static int humanStrength(Player p, HumanStrength k) {return get(p).humanStrengthCount(k);}
    public static boolean hasBranch(Player p, StrengthBranch b) {return get(p).hasBranch(b);}

    //region what the body can actually bring to bear [承受上限]
    public static int capacity(Player p) {
        return ApertureService.aperture(p).extremePhysique().getStrengthCapacity();
    }

    public static boolean isUnleashed(Player p) {return p.hasEffect(ModEffects.ALL_OUT_EFFORT);}

    public static int usableJin(Player p) {
        int total = get(p).totalJin();
        return isUnleashed(p) ? total : usableJin(capacity(p), total);
    }

    public static int usableJin(int capacity, int total) {
        if (total <= capacity) return total;

        int span = capacity * (LOCK_MULTIPLE - 1);
        return capacity + OVERFLOW_JIN * Math.min(total - capacity, span) / span;
    }
    //endregion

    public static void grant(ServerPlayer p, BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(ServerPlayer p, BeastStrength b) {store(p, get(p).without(b));}
    public static void clear(ServerPlayer p) {store(p, StrengthData.DEFAULT);}
    private static void store(ServerPlayer p, StrengthData d) {
        p.setData(ModAttachments.STRENGTH, d);
        AttackService.refresh(p);
    }

    public static void setHumanStrength(ServerPlayer p, HumanStrength k, int v) {
        store(p, get(p).withHumanStrength(k, v));
    }
    public static void addHumanStrength(ServerPlayer p, HumanStrength k, int d) {
        setHumanStrength(p, k, humanStrength(p, k) + d);
    }
}

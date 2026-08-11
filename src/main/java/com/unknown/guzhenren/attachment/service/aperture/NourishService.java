package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Nourishing the Aperture [温养空窍] and striking its wall [冲击窍壁] -- the only way a rank rises.
 *
 * <p>☠ A rank-up must set the stage back to the lowest one. Raising the rank alone leaves the old
 * stage in place, which reads as an impossible cultivation and multiplies the essence cap by itself.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class NourishService {

    private NourishService() {}

    public static final int PERCENT_PER_SECOND = 1;
    public static final int COST_DIVISOR = 100;
    public static final int BASE_LOSS_MIN = 1;
    public static final int BASE_LOSS_MAX = 5;

    /** One strike costs one and a half of a Ten-Extremes peak pool, so no pool can ever hold it. */
    public static final long IMPACT_COST_PER_RANK_BASE = 1_200L;

    private static final String STARVED = "guzhenren.nourish.starved";
    private static final String STAGE_UP = "guzhenren.nourish.stage_up";
    private static final String IMPACT_POOR = "guzhenren.impact.poor";
    private static final String IMPACT_SUCCESS = "guzhenren.impact.success";
    private static final String IMPACT_HOLD = "guzhenren.impact.hold";
    private static final String IMPACT_DROP_STAGE = "guzhenren.impact.drop_stage";
    private static final String IMPACT_DROP_BASE = "guzhenren.impact.drop_base";

    /** What one strike against the aperture wall did. */
    public enum Outcome {SUCCESS, HOLD, DROP_STAGE, DROP_BASE}

    public static NourishData get(Player p) {return p.getData(ModAttachments.NOURISH);}
    public static boolean isCultivating(Player p) {return get(p).cultivating();}
    public static float fraction(Player p) {return get(p).fraction();}

    //region what the screen asks
    public static boolean canNourish(Player p) {
        return ApertureService.isAwakened(p) && !isCultivating(p) && !atCeiling(p) && !get(p).isFull();
    }
    public static boolean canImpact(Player p) {
        Aperture a = ApertureService.aperture(p);
        return ApertureService.isAwakened(p) && !isCultivating(p) && get(p).isFull()
                && a.stage() == Stage.HIGHEST && a.rank() != Rank.HIGHEST;
    }
    private static boolean atCeiling(Player p) {
        Aperture a = ApertureService.aperture(p);
        return a.rank() == Rank.HIGHEST && a.stage() == Stage.HIGHEST;
    }
    //endregion

    public static long costPerSecond(Player p) {
        long max = EssenceService.maxEssence(p);
        return Math.max(1L, (max + COST_DIVISOR - 1) / COST_DIVISOR);
    }
    public static long impactCost(Player p) {
        return IMPACT_COST_PER_RANK_BASE * ApertureService.aperture(p).rank().getRankBase();
    }
    public static boolean canAffordImpact(Player p) {return PrimevalStoneItem.canAfford(p, impactCost(p));}

    public static void start(ServerPlayer player) {
        if (!canNourish(player)) return;
        store(player, get(player).withCultivating(true).withStarvedSince(NourishData.NOT_STARVED));
    }

    public static void cancel(ServerPlayer player) {
        NourishData data = get(player);
        if (!data.cultivating()) return;
        store(player, data.withCultivating(false).withStarvedSince(NourishData.NOT_STARVED));
    }

    //region the second that the heartbeat bills
    public static void tickNourish(ServerPlayer player) {
        NourishData data = get(player);
        if (!data.cultivating()) return;
        if (!ApertureService.isAwakened(player) || atCeiling(player)) {cancel(player); return;}

        player.setDeltaMovement(Vec3.ZERO);

        long cost = costPerSecond(player);
        if (!pay(player, cost)) {
            long now = player.level().getGameTime();
            NourishData starving = data.isStarving() ? data : data.withStarvedSince(now);
            if (starving.starvedOut(now)) {
                store(player, starving.withCultivating(false).withStarvedSince(NourishData.NOT_STARVED));
                player.displayClientMessage(Component.translatable(STARVED), true);
                return;
            }
            store(player, starving);
            return;
        }

        NourishData fed = data.withStarvedSince(NourishData.NOT_STARVED)
                .withProgress(data.progress() + PERCENT_PER_SECOND);
        if (!fed.isFull()) {store(player, fed); return;}

        Stage stage = ApertureService.aperture(player).stage();
        if (stage == Stage.HIGHEST) {
            store(player, fed.withCultivating(false));
            return;
        }
        ApertureService.setStage(player, stage.shift(1));
        store(player, NourishData.DEFAULT);
        player.displayClientMessage(Component.translatable(STAGE_UP), true);
    }

    private static boolean pay(ServerPlayer player, long cost) {
        PrimevalStoneItem.topUp(player);
        return EssenceService.consume(player, cost);
    }
    //endregion

    //region striking the wall
    public static void impactWall(ServerPlayer player) {
        if (!canImpact(player)) return;
        Aperture a = ApertureService.aperture(player);
        long cost = impactCost(player);
        if (!PrimevalStoneItem.spend(player, cost)) {
            player.displayClientMessage(Component.translatable(IMPACT_POOR, cost), true);
            return;
        }

        int roll = player.getRandom().nextInt(100);
        Outcome outcome = resolve(roll, a.isExtreme());

        switch (outcome) {
            case SUCCESS -> {
                ApertureService.setRank(player, a.rank().shift(1));
                ApertureService.setStage(player, Stage.LOWEST);
                say(player, IMPACT_SUCCESS);
            }
            case HOLD -> say(player, IMPACT_HOLD);
            case DROP_STAGE -> {
                ApertureService.setStage(player, a.stage().shift(-1));
                say(player, IMPACT_DROP_STAGE);
            }
            case DROP_BASE -> {
                int loss = BASE_LOSS_MIN + player.getRandom().nextInt(BASE_LOSS_MAX - BASE_LOSS_MIN + 1);
                ApertureService.setBaseEssence(player, a.baseEssence() - loss);
                say(player, IMPACT_DROP_BASE);
            }
        }
        store(player, NourishData.DEFAULT);
    }

    /**
     * The seam the unit tests pin: a roll of {@code 0..99} against the two outcome tables.
     * ☠ The two tables split at different points, and only the Ten-Extremes one can never lose base.
     */
    public static Outcome resolve(int roll, boolean extreme) {
        if (extreme) {
            if (roll < 60) return Outcome.SUCCESS;
            if (roll < 85) return Outcome.HOLD;
            return Outcome.DROP_STAGE;
        }
        if (roll < 40) return Outcome.SUCCESS;
        if (roll < 70) return Outcome.HOLD;
        if (roll < 90) return Outcome.DROP_STAGE;
        return Outcome.DROP_BASE;
    }
    //endregion

    private static void say(ServerPlayer p, String key) {p.displayClientMessage(Component.translatable(key), true);}
    private static void store(ServerPlayer p, NourishData d) {p.setData(ModAttachments.NOURISH, d);}
}

package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureStatus;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Nourishing the Aperture [温养空窍] and striking its wall [冲击窍壁] -- the only way a rank rises.
 *
 * <p>Static service over the {@code nourish_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Two client-intent entry points ({@code start}, {@code impactWall}) driven by
 * G-panel buttons, plus {@code tickNourish} on the heartbeat. The strike cost is paid in 元石 via
 * {@link PrimevalStoneItem#spend(ServerPlayer, long)}, never via the essence pool -- it is 1.5× a
 * ten-extreme peak pool by construction, so no pool can hold it.
 *
 * <p>⚠ A rank-up MUST also set the stage back to {@code LOWEST} -- {@code setRank} leaves the stage
 * alone, so the missing call yields a "二转巅峰" that squares the essence cap. ⚠ The strike zeroes
 * progress whether it succeeds or fails; that IS how "you must run a whole peak round again" is
 * implemented, with no separate flag. ⚠ Charge the strike BEFORE rolling, and leave the progress
 * alone when the charge fails -- a click nobody can afford must cost nothing. ⚠ A hastened clock
 * bills MORE seconds per heartbeat ({@code steps}), never a bigger second -- scaling the progress and
 * the price instead would round the round's length off the pool it is defined to cost.
 *
 * @author Alex
 * @version 1.0.0
 * @see ApertureService
 * @see TimeFlowService
 * @since 1.0.0
 */

public final class NourishService {

    private NourishService() {}

    public static final int PERCENT_PER_SECOND = 1;
    public static final int COST_DIVISOR = 100;
    public static final int BASE_LOSS_MIN = 1;
    public static final int BASE_LOSS_MAX = 5;

    /**
     * One strike costs one and a half of a Ten-Extremes peak pool, so no pool can ever hold it.
     */
    public static final long IMPACT_COST_PER_RANK_BASE = 1_200L;

    /**
     * Where the pressure gauge lands after a petrified aperture converts a full one into a rank-up.
     */
    public static final int CONVERTED_PRESSURE = 90;

    private static final String STARVED = "guzhenren.nourish.starved";
    private static final String STAGE_UP = "guzhenren.nourish.stage_up";
    private static final String IMPACT_POOR = "guzhenren.impact.poor";
    private static final String IMPACT_SUCCESS = "guzhenren.impact.success";
    private static final String IMPACT_HOLD = "guzhenren.impact.hold";
    private static final String IMPACT_DROP_STAGE = "guzhenren.impact.drop_stage";
    private static final String IMPACT_DROP_BASE = "guzhenren.impact.drop_base";

    /**
     * What one strike against the aperture wall did.
     */
    public enum Outcome {SUCCESS, HOLD, DROP_STAGE, DROP_BASE}

    public static @NotNull NourishData get(@NotNull Player p) {return p.getData(ModAttachments.NOURISH);}
    public static boolean isCultivating(@NotNull Player p) {return get(p).cultivating();}
    public static boolean isPetrified(@NotNull Player p) {return get(p).petrified();}
    public static float fraction(@NotNull Player p) {return get(p).fraction();}

    //region what the screen asks
    public static boolean canNourish(@NotNull Player p) {
        return ApertureService.isAwakened(p) && !isCultivating(p)
                && ApertureService.status(p) == ApertureStatus.NORMAL
                && !atCeiling(p) && !get(p).isFull();
    }
    public static boolean canImpact(@NotNull Player p) {
        Aperture a = ApertureService.aperture(p);
        return ApertureService.isAwakened(p) && !isCultivating(p)
                && ApertureService.status(p) == ApertureStatus.NORMAL && get(p).isFull()
                && a.stage() == Stage.HIGHEST && a.rank() != Rank.HIGHEST;
    }
    private static boolean atCeiling(Player p) {
        Aperture a = ApertureService.aperture(p);
        return a.rank() == Rank.HIGHEST && a.stage() == Stage.HIGHEST;
    }
    //endregion

    public static long costPerSecond(@NotNull Player p) {
        long max = EssenceService.maxEssence(p);
        return Math.max(1L, (max + COST_DIVISOR - 1) / COST_DIVISOR);
    }
    public static long impactCost(@NotNull Player p) {
        return IMPACT_COST_PER_RANK_BASE * ApertureService.aperture(p).rank().getRankBase();
    }
    public static boolean canAffordImpact(@NotNull Player p) {return PrimevalStoneItem.canAfford(p, impactCost(p));}

    public static void start(@NotNull ServerPlayer player) {
        if (!canNourish(player)) return;
        store(player, get(player).withCultivating(true).withStarvedSince(NourishData.NOT_STARVED));
    }

    public static void cancel(@NotNull ServerPlayer player) {
        NourishData data = get(player);
        if (!data.cultivating()) return;
        store(player, data.withCultivating(false).withStarvedSince(NourishData.NOT_STARVED));
    }

    //region 温养 [nourishing] -- the second that the heartbeat bills
    /**
     * ⚠ A hastened clock bills MORE seconds per heartbeat, never a bigger second. Scaling the progress
     * and the price instead would round the round's length off the pool it is defined to cost.
     */
    public static void tickNourish(@NotNull ServerPlayer player) {
        for (int second = TimeFlowService.steps(player); second > 0; second--) {
            if (!nourishSecond(player)) return;
        }
    }

    @SuppressWarnings("resource")
    private static boolean nourishSecond(ServerPlayer player) {
        NourishData data = get(player);
        if (!data.cultivating()) return false;
        if (ApertureService.status(player) != ApertureStatus.NORMAL
                || !ApertureService.isAwakened(player) || atCeiling(player)) {cancel(player); return false;}

        player.setDeltaMovement(Vec3.ZERO);

        long cost = costPerSecond(player);
        if (!pay(player, cost)) {
            long now = player.level().getGameTime();
            NourishData starving = data.isStarving() ? data : data.withStarvedSince(now);
            if (starving.starvedOut(now)) {
                store(player, starving.withCultivating(false).withStarvedSince(NourishData.NOT_STARVED));
                player.displayClientMessage(Component.translatable(STARVED), true);
                return false;
            }
            store(player, starving);
            return false;
        }

        NourishData fed = data.withStarvedSince(NourishData.NOT_STARVED)
                .withProgress(data.progress() + PERCENT_PER_SECOND);
        if (!fed.isFull()) {store(player, fed); return true;}

        Stage stage = ApertureService.aperture(player).stage();
        if (stage == Stage.HIGHEST) {
            store(player, fed.withCultivating(false));
            return false;
        }
        ApertureService.setStage(player, stage.shift(1));
        ApertureService.relievePressure(player, 20);
        store(player, NourishData.DEFAULT);
        player.displayClientMessage(Component.translatable(STAGE_UP), true);
        return false;
    }

    private static boolean pay(ServerPlayer player, long cost) {
        PrimevalStoneItem.topUp(player);
        return EssenceService.consume(player, cost);
    }
    //endregion

    //region 石窍蛊 [Stone Aperture Gu] -- straight to this rank's peak, and never further
    /**
     * ⚠ The writer that sets {@code petrified}: it lands the primary aperture on this rank's peak,
     * zeroes the pressure gauge, and locks cultivation until a full gauge converts (ten-extremes)
     * or resetAll clears it. A run in progress is force-stopped by the same write, so the heartbeat
     * loop needs no petrified check of its own.
     */
    public static void petrify(@NotNull ServerPlayer player) {
        if (get(player).petrified()) return;
        ApertureService.setStage(player, Stage.HIGHEST);
        ApertureService.setPressure(player, ApertureService.PRIMARY, 0);
        store(player, NourishData.PETRIFIED);
    }

    /**
     * The pressure gauge a petrified aperture keeps filling: at full it converts into the next
     * rank's first stage AND cures the stone -- the one way back to NORMAL short of resetAll, and
     * the landing can take the next rank's Stone Aperture Gu to enter the cycle again. Only while
     * a next rank exists; at the last rank it answers {@code false} and the caller detonates.
     */
    public static boolean convertPetrifiedPressure(@NotNull ServerPlayer player) {
        if (!isPetrified(player) || ApertureService.rank(player) == Rank.HIGHEST) return false;

        ApertureService.setRank(player, ApertureService.rank(player).shift(1));
        ApertureService.setStage(player, Stage.LOWEST);
        ApertureService.setPressure(player, ApertureService.PRIMARY, CONVERTED_PRESSURE);
        store(player, NourishData.DEFAULT);
        say(player, IMPACT_SUCCESS);
        return true;
    }
    //endregion

    //region striking the wall
    public static void impactWall(@NotNull ServerPlayer player) {
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
                ApertureService.relievePressure(player, 50);
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
    public static @NotNull Outcome resolve(int roll, boolean extreme) {
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

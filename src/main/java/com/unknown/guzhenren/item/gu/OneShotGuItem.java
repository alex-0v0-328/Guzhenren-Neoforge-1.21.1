package com.unknown.guzhenren.item.gu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A one-shot Gu [一次性]: refining and using are one act, so it carries no state and it stacks.
 *
 * <p>⚠ Its charge length is fixed here. A Gu whose ritual needs a longer bar cannot extend this class
 * and has to sit directly under {@link MortalGuItem} instead.
 *
 * @author Alex
 * @since 1.0.0
 */
public abstract class OneShotGuItem extends MortalGuItem {

    private static final String TOOLTIP_REFINE_COST = "guzhenren.item.gu.refine_cost";

    public static final int REFINE_TICKS = 20;
    private static final int STEP_TICKS = 5;

    protected OneShotGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    //region the one press
    @Override
    protected final @Nullable Refusal gate(Player player, ItemStack stack) {
        Refusal cost = essenceGate(player, refineCost(), FAILED_REFINE_ESSENCE);
        return cost != null ? cost : useGate(player, stack);
    }

    @Override
    protected final int apply(ServerPlayer player, ItemStack stack) {
        payRefineCost(player);
        return useApply(player, stack);
    }

    @Override
    protected final int useDurationTicks(Player player, ItemStack stack) {return REFINE_TICKS;}
    //endregion

    //region display
    @Override
    public Component chargeCaption(ItemStack stack, int remainingTicks) {return refineCaptionPlain();}

    @Override
    public Float chargeFraction(ItemStack stack, int remainingTicks) {
        int steps = Math.ceilDiv(REFINE_TICKS - remainingTicks, STEP_TICKS);
        return steps * STEP_TICKS / (float) REFINE_TICKS;
    }

    @Override
    protected @Nullable MutableComponent progressLine(ItemStack stack) {
        return refineCost() > 0 ? Component.translatable(TOOLTIP_REFINE_COST, refineCost()) : null;
    }
    //endregion
}

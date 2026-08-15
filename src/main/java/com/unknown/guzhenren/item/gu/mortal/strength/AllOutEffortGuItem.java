package com.unknown.guzhenren.item.gu.mortal.strength;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * All-Out Effort Gu [全力以赴蛊]: for a while, the body's carrying limit [承受上限] stops applying.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.TendedGuItem}. Three rungs register against this one
 * class (三转..五转). The payout stamps a marker effect and calls
 * {@link com.unknown.guzhenren.attachment.service.body.AttackService#refresh}; the lift itself is read
 * back by the strength service, so attack still comes out of one formula.
 *
 * <p>⚠ Its effect is a marker carrying no {@code AttributeModifier}. A modifier would be a second
 * source for one fact, and re-using it while it runs is a refusal.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.item.gu.TendedGuItem
 */
public class AllOutEffortGuItem extends TendedGuItem {

    private static final String FAILED_ALREADY_UNLEASHED = "guzhenren.item.failed.all_out_active";

    private final int effectSeconds;

    public AllOutEffortGuItem(Properties properties, int effectSeconds, GuSpec spec) {
        super(properties, spec);
        this.effectSeconds = effectSeconds;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return player.hasEffect(ModEffects.ALL_OUT_EFFORT) ? new Refusal(FAILED_ALREADY_UNLEASHED) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(ModEffects.instance(ModEffects.ALL_OUT_EFFORT, effectSeconds * Ticks.SECOND, tier()));
        AttackService.refresh(player);
    }
}

package com.unknown.guzhenren.item.gu.mortal.wisdom;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.effect.timed.CasualThoughtEffect;
import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The Casual Gu [随意蛊]: a one-use wisdom Gu that floods the mind with random thoughts for ten seconds.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.ConsumedGuItem}, making it tended AND taken by its
 * own use. The effect holder comes from registration; the amplifier is derived from the rank so a
 * higher-rung Gu floods harder. The payout delegates to
 * {@link com.unknown.guzhenren.registry.ModEffects#instance}.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.item.gu.ConsumedGuItem
 */
public class CasualGuItem extends ConsumedGuItem {

    private final Holder<MobEffect> effect;

    public CasualGuItem(Properties properties, Holder<MobEffect> effect, GuSpec spec) {
        super(properties, spec);
        this.effect = effect;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {return null;}

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        int amplifier = spec.rank().ordinal() - Rank.ONE.ordinal();
        player.addEffect(ModEffects.instance(effect, CasualThoughtEffect.DURATION_TICKS, amplifier));
    }
}

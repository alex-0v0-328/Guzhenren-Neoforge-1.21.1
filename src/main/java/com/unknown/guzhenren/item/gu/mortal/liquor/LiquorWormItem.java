package com.unknown.guzhenren.item.gu.mortal.liquor;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureEssenceService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Liquor Worm [酒虫]: a tended Gu that distills ordinary essence [真元] into the distilled reserve, in phases.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.TendedGuItem}. Four rungs register against this one
 * class, each usable only at its own rank. The payout calls
 * {@link com.unknown.guzhenren.attachment.service.aperture.ApertureEssenceService#beginDistilling} and stamps a
 * day-long effect; the three phases (drain, redirect, 1:2 spend) live in the service and the effect,
 * not here.
 *
 * <p>⚠ While it runs the ordinary pool is empty by design. Anything gating on essence must ask for the
 * spendable figure, or it will refuse everything for the whole of that stretch.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.TendedGuItem
 * @since 1.0.0
 */

public class LiquorWormItem extends TendedGuItem {

    private static final String FAILED_RANK = "guzhenren.item.failed.liquor_rank";
    private static final String FAILED_DISTILLING = "guzhenren.item.failed.liquor_distilling";

    public LiquorWormItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK, Component.translatable(rank().getTranslationKey()));
        }
        return ApertureEssenceService.canDistill(player) ? null : new Refusal(FAILED_DISTILLING);
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        ApertureEssenceService.beginDistilling(player);
        player.addEffect(ModEffects.instance(ModEffects.LIQUOR_WORM, Ticks.DAY, tier()));
    }
}

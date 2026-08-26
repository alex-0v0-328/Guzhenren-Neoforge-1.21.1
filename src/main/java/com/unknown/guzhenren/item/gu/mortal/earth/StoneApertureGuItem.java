package com.unknown.guzhenren.item.gu.mortal.earth;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Stone Aperture Gu [石窍蛊]: the aperture it is used on lands on this rank's peak, then turns to
 * stone -- never nourished, never struck, again.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.ConsumedGuItem}, so it is tended, never feeds and
 * is taken by its own use. Three rungs register against this one class; the gate refuses a rank
 * mismatch, an already-petrified holder, and a holder already at the peak (that use would buy only
 * the lock). The payout delegates to {@link
 * com.unknown.guzhenren.attachment.service.aperture.NourishService#petrify}.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.ConsumedGuItem
 * @since 1.0.0
 */

public class StoneApertureGuItem extends ConsumedGuItem {

    private static final String FAILED_RANK_MISMATCH = "guzhenren.item.failed.rank_mismatch";
    private static final String FAILED_PETRIFIED = "guzhenren.item.failed.aperture_petrified";
    private static final String FAILED_STAGE_PEAK = "guzhenren.item.failed.stage_peak";

    public StoneApertureGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK_MISMATCH, Component.translatable(rank().getTranslationKey()));
        }
        if (NourishService.isPetrified(player)) return new Refusal(FAILED_PETRIFIED);
        return ApertureService.stage(player) == Stage.HIGHEST ? new Refusal(FAILED_STAGE_PEAK) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        NourishService.petrify(player);
    }
}

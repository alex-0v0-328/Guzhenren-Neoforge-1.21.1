package com.unknown.guzhenren.item.gu.mortal.human;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Second Aperture Gu [第二空窍蛊]: the only opener of the second aperture, human path.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.ConsumedGuItem}, so it is tended, feeds on primeval
 * stones, and is taken by its own use. Five rungs register against this one class. The gate refuses a
 * holder with no aperture, a rung above the primary's rank while only the primary exists, and a second
 * aperture already at or above the rung -- an upgrade must be strictly higher than the second aperture
 * alone, and it overwrites it whole (back to the first stage, Grade-A at 8/10, progress zeroed, bound
 * paths kept). Usable while undead: an aperture opened by it reads a LIVING one under every later
 * undeath.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.ConsumedGuItem
 * @since 1.0.0
 */

public class SecondApertureGuItem extends ConsumedGuItem {

    private static final String FAILED_ABSENT = "guzhenren.item.failed.aperture_absent";
    private static final String FAILED_RANK_MISMATCH = "guzhenren.item.failed.rank_mismatch";
    private static final String FAILED_SECOND_RANK = "guzhenren.item.failed.second_aperture_rank";
    public SecondApertureGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        ApertureData data = ApertureService.get(player);
        if (!data.isAwakened()) return new Refusal(FAILED_ABSENT);
        if (data.count() <= ApertureData.SECONDARY
                && data.primary().rank().ordinal() < rank().ordinal()) {
            return new Refusal(FAILED_RANK_MISMATCH, Component.translatable(rank().getTranslationKey()));
        }
        if (data.count() > ApertureData.SECONDARY
                && data.get(ApertureData.SECONDARY).rank().ordinal() >= rank().ordinal()) {
            return new Refusal(FAILED_SECOND_RANK);
        }
        return null;
    }
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        ApertureService.openSecondary(player, rank());
    }
}

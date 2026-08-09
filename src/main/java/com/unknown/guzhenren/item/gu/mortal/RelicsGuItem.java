package com.unknown.guzhenren.item.gu.mortal;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.OneShotGuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RelicsGuItem extends OneShotGuItem {

    private static final String FAILED_RANK_MISMATCH = "guzhenren.item.failed.rank_mismatch";
    private static final String FAILED_STAGE_PEAK = "guzhenren.item.failed.stage_peak";

    public RelicsGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected @Nullable Refusal useGate(Player player, ItemStack stack) {
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK_MISMATCH, Component.translatable(rank().getTranslationKey()));
        }
        return ApertureService.stage(player) == Stage.HIGHEST ? new Refusal(FAILED_STAGE_PEAK) : null;
    }

    @Override
    protected int useApply(ServerPlayer player, ItemStack stack) {
        ApertureService.shiftStage(player, 1);
        return 1;
    }
}

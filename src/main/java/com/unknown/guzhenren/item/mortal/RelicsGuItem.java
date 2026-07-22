package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.MortalGuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Relics Gu [舍利蛊]: raises the stage by one. One class, five items -- registration gives the rank.
//  ⚠ "Never crosses rank" needs no guard: Stage.shift stops at the peak, and breaking through is a mechanic.
public class RelicsGuItem extends MortalGuItem {

    private static final String FAILED_RANK_MISMATCH = "guzhenren.item.failed.rank_mismatch";
    private static final String FAILED_STAGE_PEAK = "guzhenren.item.failed.stage_peak";

    public RelicsGuItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.HEAVEN, false, false);
    }

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        //  ⚠ An unawakened player reads Rank.NONE, so this gate catches him too -- no isAwakened needed.
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK_MISMATCH, Component.translatable(rank().getTranslationKey()));
        }
        if (ApertureService.stage(player) == Stage.HIGHEST) {
            return new Refusal(FAILED_STAGE_PEAK);
        }
        return null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        ApertureService.shiftStage(player, 1);
        return reusable() ? 0 : 1;
    }
}

package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.MortalGuItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Hope Gu [希望蛊]: awakens on use, once and never again. The essence bar appearing IS the success.
//  ⚠ ApertureService.awaken appends without asking -- this gate is all that refuses a second aperture.
public class HopeGuItem extends MortalGuItem {

    private static final String FAILED_AWAKENED = "guzhenren.item.failed.awakened";

    //  Rank I, Human Path, single-use, no feeding -- what this Gu is, not what registration picked.
    public HopeGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.HUMAN, false, false);
    }

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        return ApertureService.isAwakened(player) ? new Refusal(FAILED_AWAKENED) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        ApertureService.awaken(player);
        //  ⚠ Awakening flips sourceAwakened -- without this /gzr aperture lags a relog.
        ModCommandSupport.refreshCommands(player);
        return reusable() ? 0 : 1;
    }
}

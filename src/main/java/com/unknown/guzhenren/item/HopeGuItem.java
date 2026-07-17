package com.unknown.guzhenren.item;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

//  希望蛊: 右键开窍, 一次而已. The essence bar appearing IS the success -- see CLAUDE.md "Client".
//  ⚠ ApertureService.awaken appends without asking, so this gate is the only thing refusing a 第二空窍.
public class HopeGuItem extends MortalGuItem {

    private static final String FAILED_AWAKENED = "guzhenren.item.failed.awakened";

    //  一转人道, 一次消耗, 无需喂食 -- what 希望蛊 is, not what registration picked.
    public HopeGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.HUMAN, false, false);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (ApertureService.isAwakened(player)) {
            if (player instanceof ServerPlayer server) refuse(server, FAILED_AWAKENED);
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer server) {
            ApertureService.awaken(server);
            //  ⚠ 开窍 flips sourceAwakened -- without this /gzr aperture lags a relog.
            ModCommandSupport.refreshCommands(server);
            spend(server, stack, reusable() ? 0 : 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

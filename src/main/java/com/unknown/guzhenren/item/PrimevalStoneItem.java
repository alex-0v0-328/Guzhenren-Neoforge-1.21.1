package com.unknown.guzhenren.item;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  元石: 右键回复真元, 一次吃够填满所需的颗数. Refuses rather than waste itself -- add() is a
//  silent no-op unawakened.
public class PrimevalStoneItem extends GuMaterialItem {

    private static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    private static final String FAILED_FULL = "guzhenren.item.failed.essence_full";

    private final long essence;

    public PrimevalStoneItem(Properties properties, long essence) {
        super(properties, Rank.ONE, GuPath.HEAVEN);
        this.essence = essence;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String failure = failure(player);
        if (failure != null) {
            if (player instanceof ServerPlayer server) refuse(server, failure);
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer server) {
            int used = used(server, stack);
            EssenceService.add(server, essence * used);
            spend(server, stack, used);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    //  How many it takes to fill up, and never more than the hand holds. ⚠ Rounds UP -- filling is the
    //  point, so the last stone may spill; Aperture's ctor clamps it away.
    private int used(Player player, ItemStack stack) {
        long deficit = EssenceService.maxEssence(player) - EssenceService.currentEssence(player);
        return (int) Math.min(stack.getCount(), (deficit + essence - 1) / essence);
    }

    //  Null when it may be used. Reads work on the client too, so both sides refuse alike.
    private static @Nullable String failure(Player player) {
        if (!ApertureService.isAwakened(player)) return FAILED_UNAWAKENED;
        return EssenceService.currentEssence(player) >= EssenceService.maxEssence(player) ? FAILED_FULL : null;
    }
}

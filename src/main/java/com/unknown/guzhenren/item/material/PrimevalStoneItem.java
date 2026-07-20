package com.unknown.guzhenren.item.material;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.GuMaterialItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Primeval Stone (元石): restores essence on use, eating as many stones as filling up takes.
//  ⚠ EssenceService.add is a silent no-op unawakened -- gate first, or the stone is eaten for nothing.
public class PrimevalStoneItem extends GuMaterialItem {

    private static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    private static final String FAILED_FULL = "guzhenren.item.failed.essence_full";

    private final long essence;

    public PrimevalStoneItem(Properties properties, long essence) {
        super(properties, Rank.ONE, GuPath.HEAVEN);
        this.essence = essence;
    }

    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
        return EssenceService.currentEssence(player) >= EssenceService.maxEssence(player)
                ? new Refusal(FAILED_FULL) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int used = used(player, stack);
        EssenceService.add(player, essence * used);
        return used;
    }

    //  ⚠ Rounds UP: filling is the point, so the last stone may spill -- Aperture's ctor clamps it away.
    private int used(Player player, ItemStack stack) {
        long deficit = EssenceService.maxEssence(player) - EssenceService.currentEssence(player);
        return (int) Math.min(stack.getCount(), (deficit + essence - 1) / essence);
    }
}

package com.unknown.guzhenren.item.material;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.gu.mortal.PrimevalElderGuItem;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Primeval stone [元石]: a right click pours its essence [真元] into the holder's aperture [空窍].
 *
 * <p>⚠ It refuses an unawakened player instead of quietly doing nothing, because the service write is
 * a no-op there and the stone would otherwise be eaten for free.
 *
 * @author Alex
 * @since 1.0.0
 */
public class PrimevalStoneItem extends GuMaterialItem {

    private static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    private static final String FAILED_FULL = "guzhenren.item.failed.essence_full";

    private final long essence;

    public PrimevalStoneItem(Properties properties, long essence) {
        super(properties, Rank.ONE, GuPath.HEAVEN);
        this.essence = essence;
    }

    public long essence() {return essence;}

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

    public int used(Player player, ItemStack stack) {
        long deficit = EssenceService.maxEssence(player) - EssenceService.currentEssence(player);
        return (int) Math.min(stack.getCount(), (deficit + essence - 1) / essence);
    }

    //region sourcing stones for something else -- the walk lives here, not in a service
    public static long pourInto(ServerPlayer player, long wanted) {
        if (wanted <= 0L) return 0L;
        long each = ((PrimevalStoneItem) ModItems.PRIMEVAL_STONE.get()).essence();
        int left = (int) Math.min(Integer.MAX_VALUE, (wanted + each - 1) / each);
        int taken = 0;

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof PrimevalElderGuItem elder) {
            int drawn = elder.drawStones(offhand, left);
            taken += drawn;
            left -= drawn;
        }
        for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof PrimevalStoneItem)) continue;
            int drawn = Math.min(left, stack.getCount());
            stack.shrink(drawn);
            taken += drawn;
            left -= drawn;
        }
        if (taken > 0) EssenceService.add(player, taken * each);
        return taken * each;
    }
    //endregion
}

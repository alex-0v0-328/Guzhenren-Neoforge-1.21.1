package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Death Qi [死气] material: free to use, and it drains lifespan [寿元] until none is left.
 *
 * <p>⚠ The debt is tallied on the body record rather than on the effect, because an effect has no
 * expiry hook to settle from. Milk and a clear command therefore cannot cure it.
 *
 * @author Alex
 * @since 1.0.0
 */
public class DeathQiItem extends QiMaterialItem {

    public DeathQiItem(Properties properties, Rank rank) {
        super(properties, rank, QiKind.DEATH);
    }

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}

    @Override
    protected long essenceCost() {return 0L;}
}

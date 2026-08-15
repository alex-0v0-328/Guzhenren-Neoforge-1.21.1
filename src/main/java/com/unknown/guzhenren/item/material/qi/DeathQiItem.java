package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Death Qi [死气] material: free to use, and it drains lifespan [寿元] until none is left.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.material.qi.QiMaterialItem}. Both the essence cost and
 * the charge duration are overridden to zero -- it is the only Qi material a mortal can use, and that
 * is exactly why: it harms him. The apply is inherited unchanged; the curse and the lifespan debt are
 * handled by {@link com.unknown.guzhenren.attachment.service.body.QiService} and
 * {@link com.unknown.guzhenren.attachment.service.body.BodyService}.
 *
 * <p>⚠ The debt is tallied on the body record rather than on the effect, because an effect has no
 * expiry hook to settle from. Milk and a clear command therefore cannot cure it.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.item.material.qi.QiMaterialItem
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

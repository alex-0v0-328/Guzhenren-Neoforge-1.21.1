package com.unknown.guzhenren.item.gu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * A tended Gu [需照顾] that its own use takes: cared for like any other, and gone once it is driven.
 *
 * <p>⚠ It is a THIRD answer to "is it still there afterwards", so the Vital [本命] slot has to ask
 * rather than test the class. Binding one would lose it, and bill its owner, on the very first click.
 *
 * @author Alex
 * @since 1.0.0
 * @see OneShotGuItem
 */
public abstract class ConsumedGuItem extends TendedGuItem {

    protected ConsumedGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    public boolean canBeVital() {return false;}

    @Override
    protected int drive(ServerPlayer player, ItemStack stack) {
        super.drive(player, stack);
        return 1;
    }
}

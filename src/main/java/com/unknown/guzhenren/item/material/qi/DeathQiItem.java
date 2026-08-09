package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DeathQiItem extends QiMaterialItem {

    public DeathQiItem(Properties properties, Rank rank) {
        super(properties, rank, QiKind.DEATH);
    }

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}

    @Override
    protected long essenceCost() {return 0L;}
}

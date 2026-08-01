package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EssenceQiItem extends QiMaterialItem {

    private static final int DURATION_TICKS = Ticks.MINUTE;

    public EssenceQiItem(Properties properties, Rank rank) {
        super(properties, rank, MarkTag.QI_ESSENCE);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);
        applyGraded(player, ModEffects.ESSENCE_QI, tier(), DURATION_TICKS);
        return spent;
    }
}

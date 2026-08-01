package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DeathQiItem extends QiMaterialItem {

    private static final int DURATION_TICKS = 72000;

    public DeathQiItem(Properties properties, Rank rank) {
        super(properties, rank, MarkTag.QI_DEATH);
    }

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);

        if (!player.hasEffect(ModEffects.DEATH_QI)) {
            player.addEffect(new MobEffectInstance(ModEffects.DEATH_QI, DURATION_TICKS, 0));
        }
        return spent;
    }
}

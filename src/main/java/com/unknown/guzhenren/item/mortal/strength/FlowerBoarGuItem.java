package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.effect.FlowerBoarGuEffect;
import com.unknown.guzhenren.item.GuSpec;
import com.unknown.guzhenren.item.TendedGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FlowerBoarGuItem extends TendedGuItem {


    public FlowerBoarGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected int useChargeTicks(Player player, ItemStack stack) {return 0;}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {return null;}

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.FLOWER_BOAR_GU, FlowerBoarGuEffect.DURATION_TICKS));
    }

}

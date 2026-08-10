package com.unknown.guzhenren.item.gu.mortal;

import com.unknown.guzhenren.effect.timed.VitalityLeafEffect;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.OneShotGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VitalityLeafGuItem extends OneShotGuItem {

    private static final String FAILED_VITALITY_ACTIVE = "guzhenren.item.failed.vitality_active";

    private static final int USE_COOLDOWN_TICKS = 20;

    public VitalityLeafGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected int cooldownTicks(ItemStack stack) {return USE_COOLDOWN_TICKS;}

    @Override
    protected @Nullable Refusal useGate(Player player, ItemStack stack) {
        return player.hasEffect(ModEffects.VITALITY_LEAF) ? new Refusal(FAILED_VITALITY_ACTIVE) : null;
    }

    @Override
    protected int useApply(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.VITALITY_LEAF, VitalityLeafEffect.DURATION_TICKS));
        return 1;
    }
}

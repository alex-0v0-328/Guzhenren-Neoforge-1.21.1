package com.unknown.guzhenren.item.gu.mortal.liquor;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LiquorWormItem extends TendedGuItem {

    private static final String FAILED_RANK = "guzhenren.item.failed.liquor_rank";
    private static final String FAILED_DISTILLING = "guzhenren.item.failed.liquor_distilling";

    public LiquorWormItem(Properties properties, GuSpec spec) {
        super(properties, spec);
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK, Component.translatable(rank().getTranslationKey()));
        }
        return EssenceService.isDistilling(player) ? new Refusal(FAILED_DISTILLING) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        EssenceService.beginDistilling(player);
        player.addEffect(new MobEffectInstance(ModEffects.LIQUOR_WORM, Ticks.DAY, tier()));
    }
}

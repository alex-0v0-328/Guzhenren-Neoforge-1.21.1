package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.effect.DeathQiEffect;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LifeQiItem extends QiMaterialItem {

    private static final String CURED = "guzhenren.item.death_qi_cured";

    private static final int[] DURATION_TICKS = {100, 300, 500, 700, 900};

    public LifeQiItem(Properties properties, Rank rank) {
        super(properties, rank, MarkTag.QI_LIFE);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);

        if (player.hasEffect(ModEffects.DEATH_QI)) {
            player.removeEffect(ModEffects.DEATH_QI);
            long refund = BodyService.refundDeathQiDebt(player,
                    DeathQiEffect.REFUND_NUMERATOR, DeathQiEffect.REFUND_DENOMINATOR);
            inform(player, CURED, refund);
            return spent;
        }

        applyGraded(player, ModEffects.LIFE_QI, tier(), DURATION_TICKS[tier()]);
        return spent;
    }
}

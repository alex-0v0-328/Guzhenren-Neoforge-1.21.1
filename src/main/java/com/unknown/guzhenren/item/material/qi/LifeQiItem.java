package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.effect.pool.DeathQiEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Life Qi [生气] material: it pays down a Death Qi [死气] debt before it does anything else.
 *
 * <p>⚠ Only clearing that debt outright refunds any of the burnt lifespan [寿元]; paying it partway
 * down refunds nothing at all. The asymmetry is deliberate.
 *
 * @author Alex
 * @since 1.0.0
 */
public class LifeQiItem extends QiMaterialItem {

    private static final String CURED = "guzhenren.item.death_qi_cured";

    public LifeQiItem(Properties properties, Rank rank) {
        super(properties, rank, QiKind.LIFE);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        long death = QiService.current(player, QiKind.DEATH);
        if (death <= 0L) return super.apply(player, stack);

        long remainder = qiAmount() - Math.min(qiAmount(), death);
        QiService.add(player, QiKind.DEATH, -qiAmount());
        if (remainder > 0L) QiService.add(player, QiKind.LIFE, remainder);
        if (QiService.current(player, QiKind.DEATH) <= 0L) {
            double refund = BodyService.refundDeathQiDebt(player,
                    DeathQiEffect.REFUND_NUMERATOR, DeathQiEffect.REFUND_DENOMINATOR);
            if (refund > 0.0) inform(player, CURED, refund);
        }
        return 1;
    }
}

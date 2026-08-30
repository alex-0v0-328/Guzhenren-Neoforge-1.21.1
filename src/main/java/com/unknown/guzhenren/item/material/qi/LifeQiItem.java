package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.path.PathQiService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.effect.pool.DeathQiEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Life Qi [生气] material: it pays down a Death Qi [死气] debt before it does anything else.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.material.qi.QiMaterialItem}. The apply overrides the base to
 * route the amount into {@link com.unknown.guzhenren.attachment.service.path.PathQiService} against Death
 * Qi first; only the excess reaches the Life Qi pool. When Death Qi clears to zero the lifespan [寿元]
 * refund is handled by {@link com.unknown.guzhenren.attachment.service.body.BodyService#refundDeathQiDebt}.
 *
 * <p>⚠ Only clearing that debt outright refunds any of the burnt lifespan [寿元]; paying it partway
 * down refunds nothing at all. The asymmetry is deliberate.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.material.qi.QiMaterialItem
 * @since 1.0.0
 */

public class LifeQiItem extends QiMaterialItem {

    private static final String CURED = "guzhenren.item.death_qi_cured";
    public LifeQiItem(Properties properties, Rank rank) {
        super(properties, rank, QiKind.LIFE);
    }
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        long death = PathQiService.current(player, QiKind.DEATH);
        if (death <= 0L) return super.apply(player, stack);

        long remainder = qiAmount() - Math.min(qiAmount(), death);
        PathQiService.add(player, QiKind.DEATH, -qiAmount());
        if (remainder > 0L) PathQiService.add(player, QiKind.LIFE, remainder);
        if (PathQiService.current(player, QiKind.DEATH) <= 0L) {
            double refund = BodyService.refundDeathQiDebt(player,
                    DeathQiEffect.REFUND_NUMERATOR, DeathQiEffect.REFUND_DENOMINATOR);
            if (refund > 0.0) inform(player, CURED, refund);
        }
        return 1;
    }
}

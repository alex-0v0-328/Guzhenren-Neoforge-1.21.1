package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.effect.pool.DeathQiEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
            long refund = BodyService.refundDeathQiDebt(player,
                    DeathQiEffect.REFUND_NUMERATOR, DeathQiEffect.REFUND_DENOMINATOR);
            if (refund > 0L) inform(player, CURED, refund);
        }
        return 1;
    }
}

package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.effect.DeathQiEffect;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

//  Life Qi [生气]: health regeneration, and the one cure for Death Qi [死气].
//  ⚠ Curing and healing are EXCLUSIVE -- spending it on a curse buys the cure, never the regeneration.
public class LifeQiItem extends QiMaterialItem {

    private static final String CURED = "guzhenren.item.death_qi_cured";

    //  Seconds of regeneration by rank; the rate itself is flat, on LifeQiEffect.
    private static final int[] DURATION_TICKS = {100, 300, 500, 700, 900};

    public LifeQiItem(Properties properties, Rank rank) {
        super(properties, rank, MarkTag.QI_LIFE);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);

        //  ⚠ The cure comes first and returns: he gets his lifespan back instead of the healing, which
        //  is the whole trade. Only Life Qi refunds -- milk ends the curse but hands nothing back.
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

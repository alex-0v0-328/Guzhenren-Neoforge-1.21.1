package com.unknown.guzhenren.item.material.qi;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//  Death Qi [死气], Rank V only: lifespan bleeds until it kills, essence stops regenerating, and health
//  falls to one heart. PlayerTickEvents does the work -- the effect is only the clock.
//  ⚠ Rank V alone, by design: there is no ladder here, so nothing about it scales.
public class DeathQiItem extends QiMaterialItem {

    //  Runs until cured or fatal. ⚠ Not INFINITE_DURATION: a finite ceiling still shows a countdown, and
    //  the lifespan runs out long before this does.
    private static final int DURATION_TICKS = 72000;

    public DeathQiItem(Properties properties, Rank rank) {
        super(properties, rank, MarkTag.QI_DEATH);
    }

    //  ⚠ No charge, unlike the four beneficial ones: a curse lands on a single right-click. A mortal can
    //  drive it too (no gate), which is the point -- Death Qi harms everyone.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {return 0;}

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);

        //  ⚠ Already cursed: the specks still land and the material is still gone, but the curse does NOT
        //  refresh and does NOT deepen. However he takes it, it is the same Death Qi -- his own rule.
        if (!player.hasEffect(ModEffects.DEATH_QI)) {
            player.addEffect(new MobEffectInstance(ModEffects.DEATH_QI, DURATION_TICKS, 0));
        }
        return spent;
    }
}

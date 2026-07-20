package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.effect.VitalityLeafEffect;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Vitality Leaf Gu (生机叶蛊): one click, 64 health over 32 seconds, and the leaf is gone.
//  ⚠ No refining and no feeding, deliberately -- a recovery item earns none of that machinery.
public class VitalityLeafGuItem extends MortalGuItem {

    private static final String FAILED_VITALITY_ACTIVE = "guzhenren.item.failed.vitality_active";

    //  One second. The refusal is what actually stops the second leaf; this only stops a double click.
    private static final int USE_COOLDOWN_TICKS = 20;

    //  Rank I, Wood Path. Neither reusable nor feedable -- a leaf is spent the moment it works.
    public VitalityLeafGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.WOOD, false, false);
    }

    @Override
    protected boolean hasUse() {return true;}
    @Override
    protected int cooldownTicks(ItemStack stack) {return USE_COOLDOWN_TICKS;}

    //  ⚠ A second leaf would restart the first and waste what is left of it -- refuse while it runs.
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        return player.hasEffect(ModEffects.VITALITY_LEAF) ? new Refusal(FAILED_VITALITY_ACTIVE) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.VITALITY_LEAF, VitalityLeafEffect.DURATION_TICKS));
        return 1;
    }
}

package com.unknown.guzhenren.item.mortal.liquor;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LiquorWormItem extends RefinableGuItem {

    private static final String FAILED_RANK = "guzhenren.item.failed.liquor_rank";
    private static final String FAILED_DISTILLING = "guzhenren.item.failed.liquor_distilling";

    private static final String TOOLTIP_MEAL = "guzhenren.item.gu.meal_liquor";

    private static final int BASE_REFINE_COST = 1000;

    private static final int BASE_LIQUOR_PER_MEAL = 8;

    public LiquorWormItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.FOOD);
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, 10, tier());}

    @Override
    protected boolean usesFedClock() {return true;}

    @Override
    protected int mealItems() {return scaled(BASE_LIQUOR_PER_MEAL, 2, tier());}

    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.LIQUOR_FEED) ? 1 : 0;}

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
        player.addEffect(new MobEffectInstance(
                ModEffects.LIQUOR_WORM, Ticks.DAY, tier()));
    }

    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_MEAL, mealItems())
                : super.progressLine(stack);
    }
}

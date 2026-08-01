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

//  Liquor Worm [酒虫]: one use empties the essence [真元] pool and opens a day of distilling, during
//  which regen fills the distilled pool [精炼真元] instead and each point there spends as two.
//  ⚠ ONE class, four items -- registration gives the rank and every number falls out of it, the same
//  shape RelicsGuItem uses. A fifth rank would need no new class.
public class LiquorWormItem extends RefinableGuItem {

    private static final String FAILED_RANK = "guzhenren.item.failed.liquor_rank";
    private static final String FAILED_DISTILLING = "guzhenren.item.failed.liquor_distilling";

    private static final String TOOLTIP_MEAL = "guzhenren.item.gu.meal_liquor";

    //  Rank I's numbers -- 1,000 is 1.25× a Rank I peak Ten-Extremes pool, flat across the ladder.
    private static final int BASE_REFINE_COST = 1000;

    //  ⚠⚠ ONE meal is 8 × 2^tier bottles and buys the SAME two days at every rank (2026-08-01) -- the
    //  window is RefinableGuItem's, flat; only the price ladders. It replaced a per-day bar whose depth
    //  laddered too, which made a Rank IV worm hold sixteen days of drink.
    private static final int BASE_LIQUOR_PER_MEAL = 8;

    public LiquorWormItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.FOOD);
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, 10, tier());}

    //  ⚠⚠ The timestamp clock, not the hunger bar: fed two days, warned at two and a half, dead at three,
    //  the same at every rank. It drinks on a purpose, not on a daily nibble -- his words.
    @Override
    protected boolean usesFedClock() {return true;}

    //  8 / 16 / 32 / 64 bottles a meal.
    @Override
    protected int mealItems() {return scaled(BASE_LIQUOR_PER_MEAL, 2, tier());}

    //  Every use pays out -- there is no counting up to a grant here, the drink IS the grant.
    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.LIQUOR_FEED) ? 1 : 0;}

    //  ⚠ Rank must match EXACTLY, unlike every other refinable Gu. Refining stays open to any cultivator --
    //  standing below only lengthens the hold, which makes the nine-second bucket reachable.  CLAUDE.md.
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        if (ApertureService.rank(player) != rank()) {
            return new Refusal(FAILED_RANK, Component.translatable(rank().getTranslationKey()));
        }
        //  A second drink while the first still runs would re-empty an already empty pool and reset the
        //  clock -- pure loss. Refuse it rather than let him pay hunger for nothing.
        return EssenceService.isDistilling(player) ? new Refusal(FAILED_DISTILLING) : null;
    }

    //  Phase 1 and the clock that opens phase 2. Phase 3's close is PlayerTickEvents' job: a MobEffect
    //  has no expiry hook, and milk or death would skip one anyway.
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        EssenceService.beginDistilling(player);
        player.addEffect(new MobEffectInstance(
                ModEffects.LIQUOR_WORM, Ticks.DAY, tier()));
    }

    //  ⚠⚠ What ONE meal costs, because that is the only feeding number a tooltip CAN show: reading the
    //  clock needs the world time, and a tooltip is handed the stack alone. 「蛊饿了」 is the notice.
    //  Wild still reads 炼化 320/1000 from the base.
    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_MEAL, mealItems())
                : super.progressLine(stack);
    }
}

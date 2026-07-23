package com.unknown.guzhenren.item.mortal.liquor;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
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

    //  Rank I's numbers. ⚠ The two ladders differ on purpose: cost climbs by ten a rank so the essence
    //  wall is real, feeding only doubles so the bar never becomes unfillable.
    private static final int BASE_REFINE_COST = 1280;
    private static final int BASE_REFINE_PER_USE = 100;
    private static final int BASE_UNITS_PER_HUNGER = 4;

    private static final int MAX_HUNGER = 36;

    public LiquorWormItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.FOOD);
    }

    //  0..3 for ranks I..IV -- the exponent both ladders are built on, and the effect's amplifier.
    private int tier() {return rank().ordinal() - Rank.ONE.ordinal();}

    private static int scaled(int base, int factor, int tier) {
        int value = base;
        for (int i = 0; i < tier; i++) value *= factor;
        return value;
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, 10, tier());}

    //  ⚠ This ladder MUST track refineCost's, or rank IV would be 12,800 four-second holds -- and the
    //  "two ranks above lifts the cap" escape can't help (rank VI needed, VI..IX are 0).  CLAUDE.md "Bounds".
    @Override
    protected int refinePerUse() {return scaled(BASE_REFINE_PER_USE, 10, tier());}

    @Override
    protected int unitsPerHunger() {return scaled(BASE_UNITS_PER_HUNGER, 2, tier());}

    @Override
    protected int maxHunger() {return MAX_HUNGER;}

    //  Every use pays out -- there is no counting up to a grant here, the drink IS the grant.
    @Override
    public int usesPerGrant() {return 1;}

    //  ⚠ One use is the whole meal: it drains to the hungry mark rather than costing a point. Below that
    //  mark the floor of 1 in RefinableGuItem takes over, so the last drops still walk it down to death.
    @Override
    protected int hungerPerUse(ItemStack stack) {
        return Math.max(1, state(stack).hunger() - hungryThreshold());
    }
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.LIQUOR_FEED) ? 1 : 0;}

    //  ⚠ Rank must match EXACTLY, unlike every other refinable Gu. Refining stays open to any cultivator --
    //  standing below only lengthens the hold, which makes the nine-second bucket reachable.  CLAUDE.md.
    @Override
    protected @Nullable Refusal payoutGate(Player player) {
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
    protected void payout(ServerPlayer player) {
        EssenceService.beginDistilling(player);
        player.addEffect(new MobEffectInstance(
                ModEffects.LIQUOR_WORM, BodyService.TICKS_PER_DAY, tier()));
    }
}

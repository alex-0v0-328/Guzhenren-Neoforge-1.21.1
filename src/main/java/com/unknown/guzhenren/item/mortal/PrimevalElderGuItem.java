package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Primeval Elder Gu [元老蛊]: a vault for Primeval Stones [元石] that pays its own upkeep out of what it
//  holds. ⚠ ONE class, five items -- registration gives the rank, every number falls out of it.
public class PrimevalElderGuItem extends RefinableGuItem {

    private static final String FAILED_EMPTY = "guzhenren.item.failed.elder_gu_empty";
    private static final String TOOLTIP_STORED = "guzhenren.item.gu.stored_stones";
    private static final String HUD_WITHDRAWING = "guzhenren.hud.withdrawing";

    //  Columns -- capacity (stones it holds), then what ONE meal costs and how many days it buys.
    //  ⚠ Both meal numbers are whole, so the vault never owes a fraction of a stone or of a day.
    private static final long[] CAPACITY    = {1_000L, 10_000L, 100_000L, 1_000_000L, 100_000_000L};
    private static final int[]  MEAL_STONES = {     1,       2,        4,          8,           16};
    private static final int[]  MEAL_DAYS   = {     1,       2,        4,          6,            8};

    //  A stone is three units. ⚠ UNITS_PER_HUNGER must agree with the meal columns above (1, 1, 1, 1.33
    //  and 2 stones a day), or the aperture store's auto-feed would be a cheaper door into the same bar.
    private static final int STONE_UNITS = 3;
    private static final int[] UNITS_PER_HUNGER = {3, 3, 3, 4, 6};

    private static final int BASE_REFINE_COST = 100;
    private static final int BASE_REFINE_PER_USE = 50;
    private static final int REFINE_LADDER = 4;

    private static final int SLOW_CHARGE_TICKS = 100;
    private static final int UNCAPPED_RANK_GAP = 1;
    private static final int WITHDRAW_STONES = 64;
    private static final int WITHDRAW_TICKS = 10;

    public PrimevalElderGuItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.SPACE);
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, REFINE_LADDER, tier());}

    //  ⚠ This ladder MUST track refineCost's: left flat at 50, Rank V [五转] would be 512 three-second
    //  holds. Both climb ×4, so every rank is two.  CLAUDE.md "Liquor Worm" for the same trap.
    @Override
    protected int refinePerUse() {return scaled(BASE_REFINE_PER_USE, REFINE_LADDER, tier());}

    //  Five seconds below its rank, not the usual nine -- a vault is meant to be opened early.
    @Override
    protected int slowChargeTicks() {return SLOW_CHARGE_TICKS;}

    //  ⚠ ONE rank above already lifts the per-hold cap here, where every other Gu asks for two.
    @Override
    protected int uncappedRankGap() {return UNCAPPED_RANK_GAP;}

    @Override
    protected int unitsPerHunger() {return UNITS_PER_HUNGER[tier()];}

    //  Every use IS one withdrawal -- there is nothing to count up to.
    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    //region the vault
    public long capacity() {return CAPACITY[tier()];}
    private int mealStones() {return MEAL_STONES[tier()];}
    private int mealDays() {return MEAL_DAYS[tier()];}

    public static long stored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_STONES.get(), 0L);
    }

    private void setStored(ItemStack stack, long v) {
        stack.set(ModDataComponents.STORED_STONES.get(), Math.clamp(v, 0L, capacity()));
    }

    //  ⚠ The stone itself, not a feed tag: what it eats and what it hands back must be the same thing,
    //  and a withdrawal can only hand back one item. This rate is the auto-feed's, not the left click's.
    @Override
    protected int feedUnits(ItemStack food) {
        return food.is(ModItems.PRIMEVAL_STONE.get()) ? STONE_UNITS : 0;
    }
    //endregion

    //region depositing -- the left click
    //  ⚠ Left-click DEPOSITS here instead of feeding: the vault is what feeds this Gu, so a hand-feed
    //  would be a second door into the same bar. Silent when it cannot, as every swing is.
    @Override
    protected boolean hasSwing(Player player, ItemStack stack) {
        return refined(stack) && depositable(player, stack) > 0;
    }

    @Override
    protected int swingApply(ServerPlayer player, ItemStack stack) {
        int stones = depositable(player, stack);
        if (stones <= 0) return 0;

        //  ⚠ Creative pays no stones, exactly as it pays no food -- see RefinableGuItem.eat.
        if (!player.hasInfiniteMaterials()) player.getOffhandItem().shrink(stones);
        setStored(stack, stored(stack) + stones);
        return 0;
    }

    //  What the offhand could put in right now: what it holds, or what still fits.
    private int depositable(Player player, ItemStack stack) {
        ItemStack stones = player.getOffhandItem();
        if (!stones.is(ModItems.PRIMEVAL_STONE.get())) return 0;
        return (int) Math.min(stones.getCount(), capacity() - stored(stack));
    }
    //endregion

    //region withdrawing -- the right click
    //  ⚠ An empty vault is refused: the withdrawal would hand back nothing and still cost a day of food.
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return stored(stack) <= 0 ? new Refusal(FAILED_EMPTY) : null;
    }

    //  ⚠ Half a second flat to withdraw. The rank buckets pace the REFINING, which is a one-off; a vault
    //  that takes three seconds a stack to open is not a vault.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return refined(stack) ? WITHDRAW_TICKS : super.useDurationTicks(player, stack);
    }

    //  ⚠⚠ The vault pays for the withdrawal AT ONCE, not at the next day rollover -- at half a second a
    //  hold, 18 uses would otherwise walk a stocked Gu to 0 in nine seconds and the 19th would kill it.
    //  ⚠ Must run AFTER super, which stores its own RefinedGuState copy last; a topUp before it is lost.
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        int spent = super.apply(player, stack);
        //  1 means it just died of being forced -- an empty vault, so there is nothing to refill from.
        if (spent == 0) topUp(stack);
        return spent;
    }

    //  One stack out, or whatever is left when that is less. ⚠ Writes the vault only: apply() owns the
    //  RefinedGuState and would clobber a hunger write made here.
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        int taken = (int) Math.min(WITHDRAW_STONES, stored(stack));
        if (taken <= 0) return;

        setStored(stack, stored(stack) - taken);
        player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.PRIMEVAL_STONE.get(), taken));
    }
    //endregion

    //region the day clock
    //  ⚠ It eats out of its own vault, in whole meals, so a stocked Gu never starves however long its owner
    //  was away. An EMPTY vault is what hands the bill back to the hunger bar, where 0 still kills it.
    @Override
    public boolean decay(ServerPlayer player, ItemStack stack, long days) {
        long unpaid = days - buyDays(stack, days);
        boolean starved = unpaid > 0 && super.decay(player, stack, unpaid);
        if (!starved) topUp(stack);
        return starved;
    }

    //  Meals bought for an absence the bar alone cannot outlast -- it must end that span with a point left.
    //  Whatever they cover beyond it is credited to the bar rather than lost.
    private long buyDays(ItemStack stack, long days) {
        long shortfall = days + 1 - state(stack).hunger();
        if (shortfall <= 0) return 0;

        long meals = Math.min((shortfall + mealDays() - 1) / mealDays(), stored(stack) / mealStones());
        if (meals <= 0) return 0;

        setStored(stack, stored(stack) - meals * mealStones());
        long covered = meals * mealDays();
        if (covered > days) addDays(stack, (int) Math.min(covered - days, maxHunger()));
        return Math.min(covered, days);
    }

    //  Refills the bar one whole meal at a time while the vault can pay, which is what lands the upkeep on
    //  his own cadence: 16 stones every 8 days at Rank V [五转], never a partial meal every day.
    private void topUp(ItemStack stack) {
        while (maxHunger() - state(stack).hunger() >= mealDays() && stored(stack) >= mealStones()) {
            setStored(stack, stored(stack) - mealStones());
            addDays(stack, mealDays());
        }
    }

    private void addDays(ItemStack stack, int days) {
        store(stack, state(stack).withHunger(state(stack).hunger() + days));
    }
    //endregion

    //region display
    //  Refined it reads how full the vault is; wild, the refining line stands. ⚠ "已用 0/1" would say
    //  nothing here -- usesPerGrant is 1, and the hunger bar already shows the food.
    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_STORED, stored(stack), capacity())
                : super.progressLine(stack);
    }

    //  The charge bar over the hotbar reads the vault too, for the same reason.
    @Override
    public Component chargeCaption(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(HUD_WITHDRAWING, stored(stack), capacity())
                : super.chargeCaption(stack);
    }
    //endregion
}

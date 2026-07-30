package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Primeval Elder Gu [元老蛊]: a vault for Primeval Stones [元石] that pays its own upkeep out of what it
//  holds. ⚠ ONE class, five items -- registration gives the rank, every number falls out of it.
public class PrimevalElderGuItem extends RefinableGuItem {

    private static final String FAILED_EMPTY = "guzhenren.item.failed.elder_gu_empty";
    private static final String FAILED_NO_STONES = "guzhenren.item.failed.elder_gu_no_stones";
    private static final String TOOLTIP_STORED = "guzhenren.item.gu.stored_stones";

    //  ⚠ The capacity ladder breaks at Rank V on purpose -- 1e3/1e4/1e5/1e6 then 1e8, the user's own call.
    private static final long[] CAPACITY = {1_000L, 10_000L, 100_000L, 1_000_000L, 100_000_000L};

    //  ⚠ A MEAL is 4^tier stones buying 2^tier days -- 1/1, 4/2, 16/4, 64/8, 256/16. Both whole, so the
    //  vault never owes a fraction; the daily upkeep that falls out is 2^tier stones.
    private static final int STONE_UNITS = 3;
    private static final int MEALS_HELD = 2;

    //  50 ×10 a rank is a flat 0.0625× the peak Ten-Extremes pool of its own rank, all five rungs.
    private static final int BASE_REFINE_COST = 50;
    private static final int REFINE_LADDER = 10;

    private static final int SLOW_CHARGE_TICKS = 100;
    private static final int WITHDRAW_STONES = 64;

    public PrimevalElderGuItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.SPACE);
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, REFINE_LADDER, tier());}

    //  Five seconds below its rank, not the usual nine -- a vault is meant to be opened early.
    @Override
    protected int slowChargeTicks() {return SLOW_CHARGE_TICKS;}

    //  ⚠⚠ TWO MEALS deep, and the 饿 mark is one meal -- the same shape the Liquor Worm carries.
    @Override
    protected int maxHunger() {return MEALS_HELD * mealDays();}

    //  Stones a DAY, in units. ⚠ Must agree with the meal columns, or the aperture store's auto-feed
    //  would be a cheaper door into the same bar.
    @Override
    protected int unitsPerHunger() {return STONE_UNITS * scaled(1, 2, tier());}

    //  Every use IS one withdrawal -- there is nothing to count up to.
    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    //region the vault
    public long capacity() {return CAPACITY[tier()];}
    private int mealStones() {return scaled(1, 4, tier());}
    private int mealDays() {return scaled(1, 2, tier());}

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

    //region depositing -- the plain right click
    //  ⚠ Right-click DEPOSITS every stone he carries once this Gu is refined; while wild the same click
    //  still refines. It is FREE -- the withdrawal is the "use", so the hunger point hangs off that one.
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        if (!refined(stack)) return super.gate(player, stack);
        return depositable(player, stack) <= 0 ? new Refusal(FAILED_NO_STONES) : null;
    }

    //  ⚠ topUp after depositing, which is what 存入自动会减 means: fresh stones pay the bar down at once
    //  rather than sitting in the vault while the Gu reads hungry.
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) return super.apply(player, stack);
        deposit(player, stack);
        topUp(stack);
        return 0;
    }

    //  ⚠ Instant once refined: only the refining keeps the rank buckets. A vault that costs three seconds
    //  a stack to open is not a vault.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return refined(stack) ? 0 : super.useDurationTicks(player, stack);
    }

    //  Every stone in his inventory, hand included, up to what still fits.
    private int depositable(Player player, ItemStack stack) {
        long room = capacity() - stored(stack);
        if (room <= 0) return 0;

        int carried = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isStone(inventory.getItem(slot))) carried += inventory.getItem(slot).getCount();
        }
        return (int) Math.min(carried, room);
    }

    private void deposit(ServerPlayer player, ItemStack stack) {
        int wanted = depositable(player, stack);
        Inventory inventory = player.getInventory();

        for (int slot = 0, taken = 0; slot < inventory.getContainerSize() && taken < wanted; slot++) {
            ItemStack stones = inventory.getItem(slot);
            if (!isStone(stones)) continue;

            int move = Math.min(stones.getCount(), wanted - taken);
            //  ⚠ Creative pays no stones, exactly as it pays no food -- see RefinableGuItem.eat.
            if (!player.hasInfiniteMaterials()) stones.shrink(move);
            taken += move;
        }
        setStored(stack, stored(stack) + wanted);
    }

    private static boolean isStone(ItemStack s) {return s.is(ModItems.PRIMEVAL_STONE.get());}
    //endregion

    //region withdrawing -- sneak + right click
    //  ⚠⚠ The withdrawal is the FRAMEWORK's use, which is why the hunger point, the payout and the vault
    //  top-up all hang off super.apply() and none of them off the deposit above.
    //  ⚠ This Gu spends its sneak click on the WITHDRAWAL, so it never gets the base's sneak-feed.
    //  Left-click still feeds it by hand, at the very rate the vault pays -- see unitsPerHunger.
    @Override
    protected boolean hasSneakUse(Player player, ItemStack stack) {return refined(stack);}

    @Override
    protected @Nullable Refusal sneakGate(Player player, ItemStack stack) {
        if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
        //  A wild Gu holds nothing, so this one check refuses it too.
        return payoutGate(player, stack);
    }

    @Override
    protected int sneakApply(ServerPlayer player, ItemStack stack) {return super.apply(player, stack);}

    //  ⚠⚠ The vault pays for the withdrawal AT ONCE, and it must happen through THIS hook rather than
    //  after super.apply() returns -- the hungry warning fires inside apply(), so a top-up any later
    //  made every single withdrawal announce 「蛊饿了」 on a bar that was about to be full again.
    //  ⚠ Never reached on the forced use that kills it: an empty vault has nothing to refill from.
    @Override
    protected void afterUse(ServerPlayer player, ItemStack stack) {topUp(stack);}

    //  ⚠ An empty vault is refused: it would hand back nothing and still cost a day of food.
    //  ⚠ Reached through sneakGate, never through the framework's gate() -- that click deposits now.
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return stored(stack) <= 0 ? new Refusal(FAILED_EMPTY) : null;
    }

    //  One stack out: the offhand if it is free, else the inventory, else the ground.
    //  ⚠ Writes the vault only -- apply() owns the RefinedGuState and would clobber a hunger write here.
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        int taken = (int) Math.min(WITHDRAW_STONES, stored(stack));
        if (taken <= 0) return;

        setStored(stack, stored(stack) - taken);
        ItemStack stones = new ItemStack(ModItems.PRIMEVAL_STONE.get(), taken);
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, stones);
            return;
        }
        //  ⚠ placeItemBackInInventory drops what does not fit, which is the third step of the rule.
        player.getInventory().placeItemBackInInventory(stones);
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
    //endregion
}

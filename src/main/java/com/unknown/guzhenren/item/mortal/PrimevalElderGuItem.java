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
    private static final String FAILED_FULL = "guzhenren.item.failed.elder_gu_full";
    private static final String FAILED_NO_STONES = "guzhenren.item.failed.elder_gu_no_stones";
    private static final String TOOLTIP_STORED = "guzhenren.item.gu.stored_stones";

    //  ⚠ The capacity ladder breaks at Rank V on purpose -- 1e3/1e4/1e5/1e6 then 1e8, the user's own call.
    private static final long[] CAPACITY = {1_000L, 10_000L, 100_000L, 1_000_000L, 100_000_000L};

    //  ⚠⚠ ONE meal is 2 × 2^tier stones and buys the SAME two days at every rank (2026-08-01): 2/4/8/16/32.
    //  The window is RefinableGuItem's, flat; only the price ladders. That works out to the very upkeep
    //  the old per-day ladder charged (1/2/4/8/16 a day), so a stocked vault lasts exactly as long.
    private static final int BASE_STONES_PER_MEAL = 2;

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

    //  ⚠⚠ The timestamp clock, not the hunger bar -- the same one the Liquor Worm runs.
    @Override
    protected boolean usesFedClock() {return true;}

    //  2 / 4 / 8 / 16 / 32 stones a meal.
    @Override
    protected int mealItems() {return scaled(BASE_STONES_PER_MEAL, 2, tier());}

    //  Every use IS one withdrawal -- there is nothing to count up to.
    @Override
    public int usesPerGrant() {return 1;}
    //endregion

    //region the vault
    public long capacity() {return CAPACITY[tier()];}

    public static long stored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_STONES.get(), 0L);
    }

    private void setStored(ItemStack stack, long v) {
        stack.set(ModDataComponents.STORED_STONES.get(), Math.clamp(v, 0L, capacity()));
    }

    //  ⚠ The stone itself, not a feed tag: what it eats and what it hands back must be the same thing,
    //  and a withdrawal can only hand back one item.
    //  ⚠ On the fed clock this is a PREDICATE, not a rate -- mealItems() is what a meal costs.
    @Override
    protected int feedUnits(ItemStack food) {
        return food.is(ModItems.PRIMEVAL_STONE.get()) ? 1 : 0;
    }
    //endregion

    //region depositing -- the plain right click
    //  ⚠ Right-click DEPOSITS every stone he carries once this Gu is refined; while wild the same click
    //  still refines. It is FREE -- the withdrawal is the "use", so the hunger point hangs off that one.
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        if (!refined(stack)) return super.gate(player, stack);
        //  ⚠ Asked FIRST: depositable() answers 0 for both reasons, and one message for two refusals told
        //  a player carrying a thousand stones that he carried none.
        if (stored(stack) >= capacity()) return new Refusal(FAILED_FULL);
        return depositable(player, stack) <= 0 ? new Refusal(FAILED_NO_STONES) : null;
    }

    //  ⚠ topUp after depositing, which is what 存入自动会减 means: fresh stones pay the bar down at once
    //  rather than sitting in the vault while the Gu reads hungry.
    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) return super.apply(player, stack);
        deposit(player, stack);
        //  ⚠ 存入自动会减: fresh stones settle whatever the clock already owes, at once.
        payOwnUpkeep(player, stack);
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

    //  ⚠⚠ drive(), NOT super.apply() -- the base's plain click feeds when its own food is in the other
    //  hand, and 元石 is exactly that, so super.apply would turn every withdrawal into a feed.
    @Override
    protected int sneakApply(ServerPlayer player, ItemStack stack) {return drive(player, stack);}

    //  ⚠ The vault settles on the heartbeat now (payOwnUpkeep), so a use needs no top-up of its own.
    //  Depositing still pays at once -- that is what 存入自动会减 means, see apply().

    //  ⚠ An empty vault is refused: it would hand back nothing and still cost a use.
    //  ⚠ Reached through sneakGate, never through the framework's gate() -- that click deposits now.
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return stored(stack) <= 0 ? new Refusal(FAILED_EMPTY) : null;
    }

    //  One stack out: the offhand if it is free, else the inventory, else the ground.
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        //  ⚠⚠ The Gu eats FIRST -- handing out the very stones that cover the meal it is about to owe
        //  is what once left a just-emptied vault announcing 「蛊饿了」. A vault too poor for one meal
        //  keeps nothing back: those last stones are his.
        long owed = needsMeal(player, stack) ? mealItems() : 0L;
        long spare = stored(stack) - owed;
        int taken = (int) Math.min(WITHDRAW_STONES, spare > 0 ? spare : stored(stack));
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

    //region its own larder
    //  ⚠⚠ It eats out of its own vault, so a stocked Gu never starves however long its owner was away --
    //  and it does so WHEREVER it sits, since this runs on the heartbeat rather than on a feeding walk.
    //  That is the only reason a Primeval Elder Gu [元老蛊] survives in a plain inventory slot, which
    //  never auto-feeds. ⚠ An EMPTY vault hands the bill straight to the clock, where 3 days still kill.
    //  ⚠ Catches up in whole meals: an absence of a week owes several, and paying one a second would
    //  charge the same stones over a week of real time.
    @Override
    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {
        while (needsMeal(player, stack) && stored(stack) >= mealItems()) {
            setStored(stack, stored(stack) - mealItems());
            //  ⚠ Advances by ONE window, never to "now" -- stamping now would silently forgive every
            //  meal the absence owed and make a long trip free.
            stack.set(ModDataComponents.FED_AT.get(), fedAt(stack) + FED_WINDOW_TICKS);
        }
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

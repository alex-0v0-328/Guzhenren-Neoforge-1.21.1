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

public class PrimevalElderGuItem extends RefinableGuItem {

    private static final String FAILED_EMPTY = "guzhenren.item.failed.elder_gu_empty";
    private static final String FAILED_FULL = "guzhenren.item.failed.elder_gu_full";
    private static final String FAILED_NO_STONES = "guzhenren.item.failed.elder_gu_no_stones";
    private static final String TOOLTIP_STORED = "guzhenren.item.gu.stored_stones";

    private static final long[] CAPACITY = {1_000L, 10_000L, 100_000L, 1_000_000L, 100_000_000L};
    private static final int BASE_STONES_PER_MEAL = 2;
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

    @Override
    protected int slowChargeTicks() {return SLOW_CHARGE_TICKS;}

    @Override
    protected boolean usesFedClock() {return true;}

    @Override
    protected int mealItems() {return scaled(BASE_STONES_PER_MEAL, 2, tier());}

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

    @Override
    protected int feedUnits(ItemStack food) {
        return food.is(ModItems.PRIMEVAL_STONE.get()) ? 1 : 0;
    }
    //endregion

    //region depositing -- the plain right click
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        if (!refined(stack)) return super.gate(player, stack);
        if (stored(stack) >= capacity()) return new Refusal(FAILED_FULL);
        return depositable(player, stack) <= 0 ? new Refusal(FAILED_NO_STONES) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) return super.apply(player, stack);
        deposit(player, stack);
        payOwnUpkeep(player, stack);
        return 0;
    }

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        return refined(stack) ? 0 : super.useDurationTicks(player, stack);
    }

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
            if (!player.hasInfiniteMaterials()) stones.shrink(move);
            taken += move;
        }
        setStored(stack, stored(stack) + wanted);
    }

    private static boolean isStone(ItemStack s) {return s.is(ModItems.PRIMEVAL_STONE.get());}
    //endregion

    //region withdrawing -- sneak + right click
    @Override
    protected boolean hasSneakUse(Player player, ItemStack stack) {return refined(stack);}

    @Override
    protected @Nullable Refusal sneakGate(Player player, ItemStack stack) {
        if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
        return payoutGate(player, stack);
    }

    @Override
    protected int sneakApply(ServerPlayer player, ItemStack stack) {return drive(player, stack);}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return stored(stack) <= 0 ? new Refusal(FAILED_EMPTY) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
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
        player.getInventory().placeItemBackInInventory(stones);
    }
    //endregion

    //region its own larder
    @Override
    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {
        while (needsMeal(player, stack) && stored(stack) >= mealItems()) {
            setStored(stack, stored(stack) - mealItems());
            stack.set(ModDataComponents.FED_AT.get(), fedAt(stack) + FED_WINDOW_TICKS);
        }
    }
    //endregion

    //region display
    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_STORED, stored(stack), capacity())
                : super.progressLine(stack);
    }
    //endregion
}

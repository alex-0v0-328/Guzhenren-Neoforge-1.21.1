package com.unknown.guzhenren.item.gu.mortal.wood;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.item.gu.mortal.PrimevalElderGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Treasure Lotus Gu [天元宝莲]: a passive that mints primeval stones [元石] and restores essence [真元].
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.TendedGuItem} but declares no clock -- it never
 * eats and never starves. The one-second heartbeat of {@code payOwnUpkeep} restores 5% of max
 * essence and mints {@code stonesPerSecond} stones: while hurt, every minted stone is banked to pay
 * the {@code stonesPerHealth} repair price; otherwise the chain is Elder Gu vaults anywhere on the
 * player, then the main bag, then the hotbar, then the offhand, then a drop. Right click is refused
 * with a {@code fail} (no swing) once refined; unrefined Gu still refine through the held channel.
 *
 * <p>⚠ The bank rides {@code HEAL_BANK}, not {@code RefinedGuState} -- every tended Gu shares that
 * record, and this state belongs to the lotus family alone. It resets when the Gu heals.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.TendedGuItem
 * @since 1.0.0
 */

public class TreasureLotusGuItem extends TendedGuItem {

    private static final int ESSENCE_REGEN_PERCENT = 5;
    private static final String FAILED_PASSIVE = "guzhenren.item.failed.no_use";

    private final int stonesPerSecond;
    private final int stonesPerHealth;

    public TreasureLotusGuItem(Properties properties, int stonesPerSecond, int stonesPerHealth, GuSpec spec) {
        super(properties, spec);
        this.stonesPerSecond = stonesPerSecond;
        this.stonesPerHealth = stonesPerHealth;
    }

    @Override
    protected boolean feedsFromOffhand() {return false;}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return new Refusal(FAILED_PASSIVE);
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {}

    //region the passive heartbeat -- 5% essence and the minting chain
    @Override
    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {
        EssenceService.add(player, EssenceService.maxEssence(player) * ESSENCE_REGEN_PERCENT / 100);
        mintStones(player, stack);
    }

    private void mintStones(ServerPlayer player, ItemStack stack) {
        int hurt = state(stack).damageTaken();
        if (hurt > 0) {
            repairFromBank(stack, hurt);
            return;
        }
        clearHealBank(stack);
        giveStones(player, stonesPerSecond);
    }

    private void repairFromBank(ItemStack stack, int hurt) {
        int bank = bankOf(stack) + stonesPerSecond;
        int healed = Math.min(hurt, bank / stonesPerHealth);
        if (healed > 0) {
            heal(stack, healed);
            bank %= stonesPerHealth;
        }
        if (state(stack).damageTaken() > 0) {
            stack.set(ModDataComponents.HEAL_BANK.get(), bank);
        } else {
            clearHealBank(stack);
        }
    }

    private int bankOf(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.HEAL_BANK.get(), 0);
    }

    private void clearHealBank(ItemStack stack) {
        stack.remove(ModDataComponents.HEAL_BANK.get());
    }

    private void giveStones(ServerPlayer player, int amount) {
        int left = fillElders(player, amount);
        if (left > 0) dropToInventory(player, left);
    }

    private int fillElders(ServerPlayer player, int amount) {
        int left = amount;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            left -= storeInElder(inventory.getItem(slot), left);
        }
        for (int aperture = 0; aperture < ApertureData.MAX_APERTURES && left > 0; aperture++) {
            List<ItemStack> storedItems = new ArrayList<>(ApertureStorageService.items(player, aperture));
            ItemStack vital = ApertureStorageService.vital(player, aperture);
            boolean storedChanged = false;
            for (ItemStack stored : storedItems) {
                if (left <= 0) break;
                int moved = storeInElder(stored, left);
                storedChanged |= moved > 0;
                left -= moved;
            }
            if (storedChanged) ApertureStorageService.set(player, aperture, storedItems);
            boolean vitalChanged = false;
            if (left > 0) {
                int moved = storeInElder(vital, left);
                vitalChanged = moved > 0;
                left -= moved;
            }
            if (vitalChanged) ApertureStorageService.setVital(player, aperture, vital);
        }
        return left;
    }

    private int storeInElder(ItemStack stack, int amount) {
        return stack.getItem() instanceof PrimevalElderGuItem elder ? elder.storeStones(stack, amount) : 0;
    }

    private void dropToInventory(ServerPlayer player, int amount) {
        ItemStack produced = new ItemStack(ModItems.PRIMEVAL_STONE.get());
        Inventory inventory = player.getInventory();
        int left = placeInto(inventory, 9, 36, produced, amount);
        left = placeInto(inventory, 0, 9, produced, left);
        ItemStack offhand = player.getOffhandItem();
        if (left > 0 && ItemStack.isSameItemSameComponents(offhand, produced)
                && offhand.getCount() < offhand.getMaxStackSize()) {
            int moved = Math.min(left, offhand.getMaxStackSize() - offhand.getCount());
            offhand.grow(moved);
            left -= moved;
        } else if (left > 0 && offhand.isEmpty()) {
            int moved = Math.min(left, produced.getMaxStackSize());
            player.setItemInHand(InteractionHand.OFF_HAND, produced.copyWithCount(moved));
            left -= moved;
        }
        while (left > 0) {
            int moved = Math.min(left, produced.getMaxStackSize());
            player.drop(produced.copyWithCount(moved), false);
            left -= moved;
        }
    }

    private int placeInto(Inventory inventory, int from, int to, ItemStack produced, int amount) {
        int left = amount;
        for (int slot = from; slot < to && left > 0; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(existing, produced)
                    && existing.getCount() < existing.getMaxStackSize()) {
                int moved = Math.min(left, existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                left -= moved;
            }
        }
        for (int slot = from; slot < to && left > 0; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                int moved = Math.min(left, produced.getMaxStackSize());
                inventory.setItem(slot, produced.copyWithCount(moved));
                left -= moved;
            }
        }
        return left;
    }
    //endregion
}

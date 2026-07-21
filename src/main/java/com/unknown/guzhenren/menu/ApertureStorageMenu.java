package com.unknown.guzhenren.menu;

import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageService;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

//  One aperture's Gu store. Unlimited in the attachment; this menu holds one page of it at a time.
//  ⚠ Paging rides vanilla's own button channel (clickMenuButton), so it needs no payload of its own.
public class ApertureStorageMenu extends AbstractContainerMenu {

    public static final int COLS = 9;
    public static final int ROWS = 6;
    public static final int PAGE_SIZE = COLS * ROWS;

    //  ⚠ Temporary pressure cap: the data is uncapped, the menu is not. 32 * 54 = 1728 slots.
    public static final int MAX_PAGES = 32;

    public static final int BUTTON_PREV = 0;
    public static final int BUTTON_NEXT = 1;

    private static final int SLOT = 18;
    private static final int STORAGE_X = 8;
    private static final int STORAGE_Y = 18;
    private static final int INVENTORY_Y = 140;
    private static final int HOTBAR_Y = 198;

    private static final int DATA_PAGE = 0;
    private static final int DATA_PAGES = 1;

    private final Player player;
    private final int aperture;
    private final SimpleContainer page = new SimpleContainer(PAGE_SIZE);

    //  ⚠ The store is not a synced attachment, so the client cannot count pages by reading it. These two
    //  ints ride vanilla's own container-data channel instead -- which is why paging needs no payload.
    private final ContainerData pageData = new SimpleContainerData(2);

    //  ⚠ Seeding the container fires slotsChanged; without this the load would immediately save itself
    //  back, and on the client that would write nothing while still costing a full rebuild.
    private boolean loading;

    public ApertureStorageMenu(int id, Inventory inventory, int aperture, int pageIndex) {
        super(ModMenus.APERTURE_STORAGE_MENU.get(), id);
        this.player = inventory.player;
        this.aperture = aperture;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addSlot(new GuSlot(page, row * COLS + col,
                        STORAGE_X + col * SLOT, STORAGE_Y + row * SLOT));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < COLS; col++) {
                addSlot(new Slot(inventory, col + row * COLS + 9,
                        STORAGE_X + col * SLOT, INVENTORY_Y + row * SLOT));
            }
        }
        for (int col = 0; col < COLS; col++) {
            addSlot(new Slot(inventory, col, STORAGE_X + col * SLOT, HOTBAR_Y));
        }

        addDataSlots(pageData);
        load(pageIndex);
    }

    public int pageIndex() {return pageData.get(DATA_PAGE);}
    public int pageCount() {return Math.max(1, pageData.get(DATA_PAGES));}
    public int aperture() {return aperture;}

    //  ⚠ Always ONE page past the last full one, or a store holding exactly 54 could never be added to;
    //  clamped to MAX_PAGES, which is what makes 32 a real ceiling and not just a display limit.
    private int countPages() {
        return Math.min(MAX_PAGES, ApertureStorageService.count(player, aperture) / PAGE_SIZE + 1);
    }

    //region paging
    //  ⚠ Vanilla routes this from ServerboundContainerButtonClickPacket -- our own channel, no payload.
    @Override
    public boolean clickMenuButton(@NotNull Player who, int id) {
        int current = pageIndex();
        int next = switch (id) {
            case BUTTON_PREV -> current - 1;
            case BUTTON_NEXT -> current + 1;
            default -> current;
        };
        //  countPages() is authoritative here: this only ever runs server-side.
        if (next < 0 || next >= countPages() || next == current) return false;

        save();
        load(next);
        broadcastChanges();
        return true;
    }

    private void load(int index) {
        loading = true;
        int at = Math.max(0, index);
        pageData.set(DATA_PAGE, at);
        pageData.set(DATA_PAGES, countPages());

        List<ItemStack> items = ApertureStorageService.items(player, aperture);
        int from = at * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int slot = from + i;
            page.setItem(i, slot < items.size() ? items.get(slot).copy() : ItemStack.EMPTY);
        }
        loading = false;
    }

    //  ⚠ Writes on every change, not only on close -- a crash mid-session must not eat the Gu.
    private void save() {
        if (loading || !(player instanceof ServerPlayer server)) return;

        List<ItemStack> window = new ArrayList<>(PAGE_SIZE);
        for (int i = 0; i < PAGE_SIZE; i++) window.add(page.getItem(i).copy());
        ApertureStorageService.setPage(server, aperture, pageIndex() * PAGE_SIZE, window);

        //  Filling the last page opens the next one, so the count has to follow every write.
        pageData.set(DATA_PAGES, countPages());
    }
    //endregion

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        save();
    }

    @Override
    public void removed(@NotNull Player who) {
        save();
        super.removed(who);
    }

    //  Shift-click: storage <-> inventory, and never into a slot that would refuse the item anyway.
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player who, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean fromStorage = index < PAGE_SIZE;

        boolean moved = fromStorage
                ? moveItemStackTo(stack, PAGE_SIZE, slots.size(), true)
                : moveItemStackTo(stack, 0, PAGE_SIZE, false);
        if (!moved) return ItemStack.EMPTY;

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player who) {return who == player && who.isAlive();}

    //  ⚠ Any Gu (MortalGuItem) belongs here -- the aperture holds them all; a Gu material
    //  (GuMaterialItem) does not. Auto-feed touches only the refinable ones. See CLAUDE.md.
    private static class GuSlot extends Slot {
        GuSlot(Container container, int index, int x, int y) {super(container, index, x, y);}

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof MortalGuItem;
        }
    }
}

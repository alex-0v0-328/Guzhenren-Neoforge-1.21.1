package com.unknown.guzhenren.client.screen;

import com.unknown.guzhenren.menu.RefinementMenu;
import com.unknown.guzhenren.recipe.GuRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RefinementScreen extends AbstractContainerScreen<RefinementMenu> {

    private static final int SLOT = RefinementMenu.SLOT;
    private static final int GRID_SLOT = RefinementMenu.GRID_SLOT;
    private static final int CELL = 16;
    private static final int INVENTORY_COLS = 9;

    private static final int ACCENT = 0xFFA1887F;
    private static final int PANEL_FILL = 0xBF000000;
    private static final int BORDER = 0x66FFFFFF;
    private static final int SLOT_FILL = 0x33FFFFFF;
    private static final int CORE_FILL = 0x4DFFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int LEGEND_TEXT = 0xFFA0A0A0;
    private static final int BUTTON_IDLE = 0x33FFFFFF;
    private static final int BUTTON_HOVER = 0x66FFFFFF;
    private static final int BUTTON_DEAD = 0x14FFFFFF;
    private static final int SHORT_RED = 0x99FF5555;
    private static final int BAR_TRACK = 0x33000000;
    private static final int BAR_WINDOW = 0xFFA1887F;
    private static final int BAR_GAP = 0x66A1887F;
    private static final int BAR_SHORT = 0xFFFF5555;

    private static final String CRAFT_KEY = "guzhenren.menu.refinement.craft";
    private static final String LEGEND_KEY = "guzhenren.menu.refinement.rings";
    private static final String WINDOW_KEY = "guzhenren.menu.refinement.window";
    private static final String GAP_KEY = "guzhenren.menu.refinement.gap";

    private static final int BACK_W = 16;
    private static final int BACK_H = 14;
    private static final String BACK_GLYPH = "<-";
    private static final int TITLE_X_WITH_BACK = 26;

    public RefinementScreen(RefinementMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 242;
        this.imageHeight = 244;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = RefinementMenu.INVENTORY_X;
        this.titleLabelX = TITLE_X_WITH_BACK;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL_FILL);
        g.renderOutline(x, y, imageWidth, imageHeight, BORDER);
        g.fill(x + 9, y + 15, x + imageWidth - 9, y + 16, ACCENT);

        drawInput(g, x, y);
        drawCell(g, x + RefinementMenu.STONE_X, y + RefinementMenu.STONE_Y, SLOT_FILL);
        drawCells(g, x + RefinementMenu.OUTPUT_X, y + RefinementMenu.OUTPUT_Y,
                RefinementMenu.OUTPUT_COLS, RefinementMenu.OUTPUT_ROWS, GRID_SLOT, SLOT_FILL);
        drawCells(g, x + RefinementMenu.INVENTORY_X, y + RefinementMenu.INVENTORY_Y,
                INVENTORY_COLS, 3, SLOT, SLOT_FILL);
        drawCells(g, x + RefinementMenu.INVENTORY_X, y + RefinementMenu.HOTBAR_Y,
                INVENTORY_COLS, 1, SLOT, SLOT_FILL);
        drawBar(g, x, y);
    }

    //region the two rings -- the 内圈 is marked by a brighter cell and an accent frame around the block
    private void drawInput(GuiGraphics g, int x, int y) {
        for (int i = 0; i < RefinementMenu.RING_SIZE; i++) {
            drawCell(g, x + RefinementMenu.ringX(i), y + RefinementMenu.ringY(i), SLOT_FILL);
        }
        drawCells(g, x + RefinementMenu.coreX(0), y + RefinementMenu.coreY(0),
                RefinementMenu.CORE_COLS, RefinementMenu.CORE_ROWS, GRID_SLOT, CORE_FILL);
        g.renderOutline(x + RefinementMenu.coreX(0) - 3, y + RefinementMenu.coreY(0) - 3,
                (RefinementMenu.CORE_COLS - 1) * GRID_SLOT + CELL + 6,
                (RefinementMenu.CORE_ROWS - 1) * GRID_SLOT + CELL + 6, ACCENT);
    }

    private void drawCells(GuiGraphics g, int x, int y, int cols, int rows, int pitch, int fill) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) drawCell(g, x + col * pitch, y + row * pitch, fill);
        }
    }

    private void drawCell(GuiGraphics g, int x, int y, int fill) {g.fill(x, y, x + CELL, y + CELL, fill);}
    //endregion

    //region the phase bar -- it empties over the 10s window, then over the 2s gap
    private void drawBar(GuiGraphics g, int x, int y) {
        int bx = x + RefinementMenu.BAR_X;
        int by = y + RefinementMenu.BAR_Y;
        g.fill(bx, by, bx + RefinementMenu.BAR_W, by + RefinementMenu.BAR_H, BAR_TRACK);
        if (!menu.running()) return;

        int span = menu.inWindow() ? GuRecipe.WINDOW_TICKS : GuRecipe.GAP_TICKS;
        int filled = RefinementMenu.BAR_W * menu.phaseLeft() / Math.max(1, span);
        g.fill(bx, by, bx + filled, by + RefinementMenu.BAR_H, barColour());
    }

    private int barColour() {
        if (!menu.inWindow()) return BAR_GAP;
        return menu.stonesIn() < menu.stonesNeeded() ? BAR_SHORT : BAR_WINDOW;
    }
    //endregion

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, ACCENT, false);
        g.drawString(font, statusLine(), RefinementMenu.INPUT_X, RefinementMenu.LEGEND_Y,
                menu.running() ? TEXT : LEGEND_TEXT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    private Component statusLine() {
        if (!menu.running()) return Component.translatable(LEGEND_KEY);

        int shown = menu.stage() + 1;
        int seconds = (menu.phaseLeft() + 19) / 20;
        return menu.inWindow()
                ? Component.translatable(WINDOW_KEY, shown, menu.stages(), seconds,
                        menu.stonesIn(), menu.stonesNeeded())
                : Component.translatable(GAP_KEY, shown, menu.stages());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderCraft(g, mouseX, mouseY);
        renderBack(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    //region the 炼制 button -- three states, because "no recipe" and "cannot pay for it" are not one thing
    private void renderCraft(GuiGraphics g, int mouseX, int mouseY) {
        int x = craftX();
        int y = craftY();
        boolean live = clickable() && menu.affords();
        boolean shortOfEssence = clickable() && !menu.affords();
        boolean hover = live && inCraft(mouseX, mouseY);

        g.fill(x, y, x + RefinementMenu.CRAFT_W, y + RefinementMenu.CRAFT_H,
                live ? (hover ? BUTTON_HOVER : BUTTON_IDLE) : BUTTON_DEAD);
        if (live || shortOfEssence) {
            g.renderOutline(x, y, RefinementMenu.CRAFT_W, RefinementMenu.CRAFT_H,
                    live ? ACCENT : SHORT_RED);
        }

        Component label = Component.translatable(CRAFT_KEY);
        g.drawString(font, label, x + (RefinementMenu.CRAFT_W - font.width(label)) / 2,
                y + (RefinementMenu.CRAFT_H - font.lineHeight) / 2 + 1, live ? TEXT : BUTTON_IDLE, false);
    }

    private boolean clickable() {return menu.ready() && !menu.running();}

    private int craftX() {return leftPos + RefinementMenu.CRAFT_X;}
    private int craftY() {return topPos + RefinementMenu.CRAFT_Y;}

    private boolean inCraft(double mx, double my) {
        return mx >= craftX() && mx < craftX() + RefinementMenu.CRAFT_W
                && my >= craftY() && my < craftY() + RefinementMenu.CRAFT_H;
    }
    //endregion

    private void renderBack(GuiGraphics g, int mouseX, int mouseY) {
        int x = backX();
        int y = backY();
        boolean hover = inBack(mouseX, mouseY);
        g.fill(x, y, x + BACK_W, y + BACK_H, hover ? BUTTON_HOVER : BUTTON_IDLE);
        g.drawString(font, BACK_GLYPH, x + (BACK_W - font.width(BACK_GLYPH)) / 2,
                y + (BACK_H - font.lineHeight) / 2 + 1, TEXT, false);
    }

    private int backX() {return leftPos + 7;}
    private int backY() {return topPos + 3;}

    private boolean inBack(double mx, double my) {
        return mx >= backX() && mx < backX() + BACK_W && my >= backY() && my < backY() + BACK_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (inBack(mx, my)) return clickBack();
            if (inCraft(mx, my) && clickable()) return clickCraft();
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean clickBack() {
        onClose();
        Minecraft.getInstance().setScreen(new PlayerInfoScreen());
        return true;
    }

    private boolean clickCraft() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return false;
        mc.gameMode.handleInventoryButtonClick(menu.containerId, RefinementMenu.BUTTON_CRAFT);
        return true;
    }
}

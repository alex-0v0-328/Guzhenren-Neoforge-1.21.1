package com.unknown.guzhenren.client.screen;

import com.unknown.guzhenren.menu.ApertureStorageMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

//  The aperture's Gu store. Same look as the info panel: no texture, 75% black, an accent line.
//  ⚠ Paging goes through clickMenuButton -- vanilla's own channel, so this screen sends no payload.
public class ApertureStorageScreen extends AbstractContainerScreen<ApertureStorageMenu> {

    private static final int SLOT = 18;
    private static final int PAGE_BUTTON_W = 16;
    private static final int PAGE_BUTTON_H = 14;
    //  Wide enough for "10 / 10" without the arrows shifting as the count grows.
    private static final int PAGE_LABEL_W = 40;

    //  The Vital Gu annex hangs off the RIGHT edge, so the panel keeps its 222 height.  CLAUDE.md.
    //  ⚠ Mirrors ApertureStorageMenu.VITAL_X -- the slot is registered there, the box is drawn here.
    private static final int VITAL_LEFT = 178;
    private static final int VITAL_RIGHT = 210;
    private static final int VITAL_BOTTOM = 44;
    private static final int VITAL_SLOT_X = 186;
    private static final int VITAL_SLOT_Y = 22;
    private static final String VITAL_KEY = "guzhenren.menu.vital";

    //  Storage green, matching the info panel's fourth tab.
    private static final int ACCENT = 0xFF81C784;
    private static final int PANEL_FILL = 0xBF000000;
    private static final int BORDER = 0x66FFFFFF;
    private static final int SLOT_FILL = 0x33FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int BUTTON_IDLE = 0x33FFFFFF;
    private static final int BUTTON_HOVER = 0x66FFFFFF;
    private static final int BUTTON_DEAD = 0x14FFFFFF;

    public ApertureStorageScreen(ApertureStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL_FILL);
        g.renderOutline(x, y, imageWidth, imageHeight, BORDER);
        //  The accent bar under the title separates header from content.
        g.fill(x + 7, y + 15, x + imageWidth - 7, y + 16, ACCENT);

        for (int row = 0; row < ApertureStorageMenu.ROWS; row++) {
            for (int col = 0; col < ApertureStorageMenu.COLS; col++) {
                int sx = x + 8 + col * SLOT;
                int sy = y + 18 + row * SLOT;
                g.fill(sx, sy, sx + 16, sy + 16, SLOT_FILL);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < ApertureStorageMenu.COLS; col++) {
                int sx = x + 8 + col * SLOT;
                int sy = y + 140 + row * SLOT;
                g.fill(sx, sy, sx + 16, sy + 16, SLOT_FILL);
            }
        }
        for (int col = 0; col < ApertureStorageMenu.COLS; col++) {
            int sx = x + 8 + col * SLOT;
            g.fill(sx, y + 198, sx + 16, y + 198 + 16, SLOT_FILL);
        }
        renderVital(g, x, y);
    }

    //  Its own little panel, same three fills as the main one, and its accent bar sits on the same
    //  line -- that alignment is what makes it read as part of the same window.
    private void renderVital(GuiGraphics g, int x, int y) {
        g.fill(x + VITAL_LEFT, y, x + VITAL_RIGHT, y + VITAL_BOTTOM, PANEL_FILL);
        g.renderOutline(x + VITAL_LEFT, y, VITAL_RIGHT - VITAL_LEFT, VITAL_BOTTOM, BORDER);
        g.fill(x + VITAL_LEFT + 4, y + 15, x + VITAL_RIGHT - 4, y + 16, ACCENT);

        Component label = Component.translatable(VITAL_KEY);
        int width = VITAL_RIGHT - VITAL_LEFT;
        g.drawString(font, label, x + VITAL_LEFT + (width - font.width(label)) / 2, y + 5, ACCENT, false);
        g.fill(x + VITAL_SLOT_X, y + VITAL_SLOT_Y,
                x + VITAL_SLOT_X + 16, y + VITAL_SLOT_Y + 16, SLOT_FILL);
    }

    //  ⚠ Only the two static labels live here. The pager is drawn in render() instead, because
    //  renderLabels' matrix is already translated to leftPos/topPos -- mixing the two coordinate
    //  systems is exactly what once put the page number underneath the "<" button.
    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, ACCENT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderPager(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    //  `< 1 / 2 >` laid out right to left, all in absolute coordinates so nothing can overlap.
    private void renderPager(GuiGraphics g, int mouseX, int mouseY) {
        renderPageButton(g, mouseX, mouseY, prevX(), menu.pageIndex() > 0, "<");
        renderPageButton(g, mouseX, mouseY, nextX(), menu.pageIndex() + 1 < menu.pageCount(), ">");

        Component page = Component.literal((menu.pageIndex() + 1) + " / " + menu.pageCount());
        g.drawString(font, page, labelX() + (PAGE_LABEL_W - font.width(page)) / 2,
                pagerY() + (PAGE_BUTTON_H - font.lineHeight) / 2 + 1, TEXT, false);
    }

    private void renderPageButton(GuiGraphics g, int mouseX, int mouseY, int x, boolean live, String glyph) {
        int y = pagerY();
        boolean hover = live && inButton(mouseX, mouseY, x);
        g.fill(x, y, x + PAGE_BUTTON_W, y + PAGE_BUTTON_H, live ? (hover ? BUTTON_HOVER : BUTTON_IDLE) : BUTTON_DEAD);
        g.drawString(font, glyph, x + (PAGE_BUTTON_W - font.width(glyph)) / 2,
                y + (PAGE_BUTTON_H - font.lineHeight) / 2 + 1, live ? TEXT : BUTTON_IDLE, false);
    }

    private int pagerY() {return topPos + 3;}
    private int nextX() {return leftPos + imageWidth - 8 - PAGE_BUTTON_W;}
    private int labelX() {return nextX() - PAGE_LABEL_W;}
    private int prevX() {return labelX() - PAGE_BUTTON_W;}

    private boolean inButton(double mx, double my, int x) {
        return mx >= x && mx < x + PAGE_BUTTON_W && my >= pagerY() && my < pagerY() + PAGE_BUTTON_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (inButton(mx, my, prevX())) return clickPage(ApertureStorageMenu.BUTTON_PREV);
            if (inButton(mx, my, nextX())) return clickPage(ApertureStorageMenu.BUTTON_NEXT);
        }
        return super.mouseClicked(mx, my, button);
    }

    //  ⚠ handleInventoryButtonClick is vanilla's own button packet -- this is why paging needs no payload.
    private boolean clickPage(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return false;
        mc.gameMode.handleInventoryButtonClick(menu.containerId, id);
        return true;
    }
}

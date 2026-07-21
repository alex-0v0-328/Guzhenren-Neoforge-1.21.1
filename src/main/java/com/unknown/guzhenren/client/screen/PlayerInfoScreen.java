package com.unknown.guzhenren.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.client.ModKeyMappings;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.InfoModel;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.network.OpenApertureStoragePayload;
import com.unknown.guzhenren.network.SetSecondaryPathPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  The G-key info panel: aperture / body / mind, plus a storage tab that opens a container instead.
//  Reads the synced attachments client-side, like the HUD. Layout notes:  CLAUDE.md "Client".
//  ⚠ WHICH rows exist is InfoModel's; this file only draws them and owns the layout.
public final class PlayerInfoScreen extends Screen {

    //  ⚠ No texture: the panel is drawn from fills, so it scales to any window.  CLAUDE.md "Client".
    private static final float SCREEN_FRACTION = 0.80F;
    private static final int PAD = 12;
    private static final int HEADER_H = 22;
    private static final int CONTENT_TOP = HEADER_H + 8;
    private static final int LINE_H = 12;

    //  The tab rail down the right edge.
    private static final int TAB_W = 76;
    private static final int TAB_H = 20;
    private static final int TAB_GAP = 4;

    private static final int PANEL_FILL = 0xBF000000;
    private static final int BORDER = 0x66FFFFFF;
    private static final int DIVIDER = 0x33FFFFFF;
    private static final int ROW_HOVER = 0x14FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TAB_IDLE = 0x26FFFFFF;
    private static final int TAB_TEXT_IDLE = 0xFFBBBBBB;
    private static final int TAB_TEXT_DEAD = 0xFF6A6A6A;

    //  ⚠ One accent per DOMAIN, never per value -- colour says "which of the three you are looking at",
    //  it never says a number is good or bad.  CLAUDE.md "Color".
    private static final int[] ACCENT = {0xFF4FC3F7, 0xFFB388FF, 0xFFFFD54F, 0xFF81C784};

    private static final String[] TAB_KEYS = {
            "guzhenren.screen.tab.aperture",
            "guzhenren.screen.tab.body",
            "guzhenren.screen.tab.mind",
            "guzhenren.screen.tab.storage",
    };

    //  The storage tab does not render rows -- it opens a container instead.
    private static final int TAB_STORAGE = 3;

    //  The secondary-path picker: 30 paths plus a "clear" cell, a grid so it needs no scrolling.
    private static final int PICK_COLS = 4;
    private static final int PICK_CELL_W = 84;
    private static final int PICK_CELL_H = 14;
    private static final int PICK_PAD = 8;

    //  A 2px hint between the values and the tab rail, drawn only when something is actually hidden.
    private static final int SCROLL_W = 2;
    private static final int SCROLL_GAP = 5;

    private int leftPos;
    private int topPos;
    private int panelW;
    private int panelH;
    private int activeTab = 0;

    //  Where the one clickable row landed this frame, or -1. ⚠ Set during render, read by mouseClicked
    //  -- the rows are rebuilt every frame, so there is no stable index to remember instead.
    private int clickableRowY = -1;
    private boolean picking;

    //  Index of the first visible row. ⚠ Clamped in render(), not here or in mouseScrolled: only render
    //  knows how many rows this tab actually built, and it rebuilds them every frame.
    private int scrollRow;

    public PlayerInfoScreen() {super(Component.translatable("guzhenren.screen.info.title"));}

    //  Sized off the window every time it opens, so a resize never leaves it stale.
    @Override
    protected void init() {
        panelW = Math.round(width * SCREEN_FRACTION);
        panelH = Math.round(height * SCREEN_FRACTION);
        leftPos = (width - panelW) / 2;
        topPos = (height - panelH) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        int right = leftPos + panelW;
        int accent = ACCENT[activeTab];

        g.fill(leftPos, topPos, right, topPos + panelH, PANEL_FILL);
        g.renderOutline(leftPos, topPos, panelW, panelH, BORDER);

        //  Header: the tab's own name in its accent, over a rule that separates it from the content.
        g.drawString(font, Component.translatable(TAB_KEYS[activeTab]),
                leftPos + PAD, topPos + (HEADER_H - font.lineHeight) / 2, accent, false);
        g.fill(leftPos + PAD, topPos + HEADER_H, right - PAD, topPos + HEADER_H + 1, DIVIDER);

        renderTabs(g, mouseX, mouseY);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || activeTab == TAB_STORAGE) return;

        List<Row> rows = rows(player);

        //  ⚠ A WINDOW over the rows, not a clipped draw: whole rows only, so nothing is ever half a line
        //  and every hit test stays exact. A full path list runs past the panel bottom without this.
        int visible = visibleRows();
        int hidden = Math.max(0, rows.size() - visible);
        scrollRow = Mth.clamp(scrollRow, 0, hidden);

        //  ⚠ Values are right-aligned against the tab rail, so numbers line up however long a label runs.
        int valueRight = tabLeft() - PAD;
        int y = contentTop();
        clickableRowY = -1;
        for (int i = scrollRow; i < Math.min(rows.size(), scrollRow + visible); i++) {
            Row row = rows.get(i);
            if (mouseY >= y - 1 && mouseY < y + LINE_H - 1 && mouseX >= leftPos && mouseX < tabLeft()) {
                g.fill(leftPos + PAD - 2, y - 1, valueRight + 2, y + LINE_H - 1, ROW_HOVER);
            }
            //  ⚠ Only a VISIBLE clickable row is clickable -- scrolled away, it leaves -1 behind.
            if (row.clickable()) clickableRowY = y;
            //  A row with no value is a section header, so it takes the accent.
            int labelColor = row.value() == null ? accent : TEXT;
            g.drawString(font, row.label(), leftPos + PAD + row.indent(), y, labelColor, false);
            if (row.value() != null) {
                g.drawString(font, row.value(), valueRight - font.width(row.value()), y, TEXT, false);
            }
            y += LINE_H;
        }
        if (hidden > 0) renderScrollBar(g, rows.size(), visible, accent);
        if (picking) renderPicker(g, mouseX, mouseY, accent);
    }

    //region scrolling
    private int contentTop() {return topPos + CONTENT_TOP;}
    private int contentBottom() {return topPos + panelH - PAD;}
    private int visibleRows() {return Math.max(0, (contentBottom() - contentTop()) / LINE_H);}

    //  Chrome, not a value -- it takes the domain accent the same way the header and active tab do.
    private void renderScrollBar(GuiGraphics g, int total, int visible, int accent) {
        int x0 = tabLeft() - SCROLL_GAP;
        int top = contentTop();
        int track = visible * LINE_H;

        g.fill(x0, top, x0 + SCROLL_W, top + track, DIVIDER);
        int thumb = Math.max(LINE_H, track * visible / total);
        int offset = (track - thumb) * scrollRow / Math.max(1, total - visible);
        g.fill(x0, top + offset, x0 + SCROLL_W, top + offset + thumb, accent);
    }

    //  ⚠ The picker swallows the wheel as well as the click, or the list would slide behind the modal.
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (picking) return true;
        if (dy == 0.0) return super.mouseScrolled(mx, my, dx, dy);

        scrollRow = Math.max(0, scrollRow - (int) Math.signum(dy));
        return true;
    }
    //endregion

    //region secondary-path picker
    //  A modal grid over the panel: every path, plus one cell that clears the choice.
    //  ⚠ Same fills as everything else -- no texture, so it scales with the window like the panel does.
    private void renderPicker(GuiGraphics g, int mouseX, int mouseY, int accent) {
        int x0 = pickLeft();
        int y0 = pickTop();
        int w = PICK_COLS * PICK_CELL_W + PICK_PAD * 2;
        int h = pickRows() * PICK_CELL_H + PICK_PAD * 2 + HEADER_H;

        g.fill(x0, y0, x0 + w, y0 + h, 0xF0000000);
        g.renderOutline(x0, y0, w, h, BORDER);
        g.drawString(font, Component.translatable("guzhenren.screen.pick.title"),
                x0 + PICK_PAD, y0 + (HEADER_H - font.lineHeight) / 2, accent, false);
        g.fill(x0 + PICK_PAD, y0 + HEADER_H, x0 + w - PICK_PAD, y0 + HEADER_H + 1, DIVIDER);

        for (int i = 0; i < pickCount(); i++) {
            int cx = x0 + PICK_PAD + (i % PICK_COLS) * PICK_CELL_W;
            int cy = y0 + PICK_PAD + HEADER_H + (i / PICK_COLS) * PICK_CELL_H;
            boolean hover = mouseX >= cx && mouseX < cx + PICK_CELL_W
                    && mouseY >= cy && mouseY < cy + PICK_CELL_H;
            if (hover) g.fill(cx, cy, cx + PICK_CELL_W, cy + PICK_CELL_H, ROW_HOVER);
            g.drawString(font, ModDisplayText.path(pickPath(i)), cx + 3,
                    cy + (PICK_CELL_H - font.lineHeight) / 2, TEXT, false);
        }
    }

    //  Index 0 clears the choice; 1.. are the paths in declaration order.
    private static int pickCount() {return GuPath.values().length + 1;}
    private static @Nullable GuPath pickPath(int i) {return i == 0 ? null : GuPath.values()[i - 1];}
    private static int pickRows() {return (pickCount() + PICK_COLS - 1) / PICK_COLS;}
    private int pickWidth() {return PICK_COLS * PICK_CELL_W + PICK_PAD * 2;}
    private int pickHeight() {return pickRows() * PICK_CELL_H + PICK_PAD * 2 + HEADER_H;}
    private int pickLeft() {return leftPos + (panelW - pickWidth()) / 2;}
    private int pickTop() {return topPos + (panelH - pickHeight()) / 2;}

    private static MutableComponent pickHint() {return Component.translatable("guzhenren.screen.pick.hint");}

    //  ⚠ Returns true even on a miss: a modal must swallow the click, or it would fall through to the
    //  tab rail underneath and switch tabs while the picker is open.
    private boolean clickPicker(double mx, double my) {
        int x0 = pickLeft() + PICK_PAD;
        int y0 = pickTop() + PICK_PAD + HEADER_H;
        for (int i = 0; i < pickCount(); i++) {
            int cx = x0 + (i % PICK_COLS) * PICK_CELL_W;
            int cy = y0 + (i / PICK_COLS) * PICK_CELL_H;
            if (mx < cx || mx >= cx + PICK_CELL_W || my < cy || my >= cy + PICK_CELL_H) continue;

            PacketDistributor.sendToServer(new SetSecondaryPathPayload(ApertureData.PRIMARY, pickPath(i)));
            picking = false;
            return true;
        }
        picking = false;
        return true;
    }
    //endregion

    //  A rail down the right edge. Active takes its domain's accent; storage greys out unawakened.
    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < TAB_KEYS.length; i++) {
            boolean active = i == activeTab;
            boolean live = tabLive(i);
            int x0 = tabLeft();
            int y0 = tabTop(i);
            boolean hover = live && !active && inTab(mouseX, mouseY, i);

            g.fill(x0, y0, x0 + TAB_W, y0 + TAB_H, active ? ACCENT[i] : TAB_IDLE);
            if (hover) g.fill(x0, y0, x0 + TAB_W, y0 + TAB_H, ROW_HOVER);
            //  A thin bar on the inner edge marks the active tab even at a glance.
            if (active) g.fill(x0 - 2, y0, x0, y0 + TAB_H, ACCENT[i]);

            Component label = Component.translatable(TAB_KEYS[i]);
            int color = active ? 0xFF101010 : live ? TAB_TEXT_IDLE : TAB_TEXT_DEAD;
            g.drawString(font, label, x0 + (TAB_W - font.width(label)) / 2,
                    y0 + (TAB_H - font.lineHeight) / 2 + 1, color, false);
        }
    }

    //  ⚠ Storage needs an aperture to live in, so an unawakened player cannot open it.
    private boolean tabLive(int tab) {
        LocalPlayer player = Minecraft.getInstance().player;
        return tab != TAB_STORAGE || (player != null && ApertureService.isAwakened(player));
    }

    private int tabLeft() {return leftPos + panelW - TAB_W - PAD;}
    private int tabTop(int i) {return topPos + PAD + i * (TAB_H + TAB_GAP);}

    private boolean inTab(double mx, double my, int i) {
        return mx >= tabLeft() && mx < tabLeft() + TAB_W && my >= tabTop(i) && my < tabTop(i) + TAB_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (picking) return button != 0 || clickPicker(mx, my);
        if (button == 0) {
            //  The one clickable row. ⚠ Its y comes from the last frame's render -- rows are rebuilt
            //  every frame, so there is no stable index to test against instead.
            if (clickableRowY >= 0 && my >= clickableRowY - 1 && my < clickableRowY + LINE_H - 1
                    && mx >= leftPos && mx < tabLeft()) {
                picking = true;
                return true;
            }
            for (int i = 0; i < TAB_KEYS.length; i++) {
                if (!inTab(mx, my, i) || !tabLive(i)) continue;
                //  ⚠ Storage is a container, not a tab of rows -- ask the server to open it instead.
                if (i == TAB_STORAGE) {
                    PacketDistributor.sendToServer(new OpenApertureStoragePayload(ApertureData.PRIMARY));
                } else {
                    activeTab = i;
                    //  A fresh tab starts at the top; the clamp alone would only fix an overshoot.
                    scrollRow = 0;
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    //  The open key toggles the panel shut too, not only Escape.
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //  ⚠ While the picker is up, both close keys close IT, not the panel -- otherwise picking a path
        //  and backing out of one are the same keystroke.
        if (picking && (keyCode == InputConstants.KEY_ESCAPE
                || ModKeyMappings.OPEN_INFO.matches(keyCode, scanCode))) {
            picking = false;
            return true;
        }
        if (ModKeyMappings.OPEN_INFO.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {return false;}

    //  The model says WHICH rows and in what order; this turns each into a drawable one.
    //  ⚠ MindHeader renders as nothing here -- the tab name is already that header. It is the one entry
    //  the two surfaces do not share, and skipping it in code beats a comment asking you to remember.
    private List<Row> rows(LocalPlayer player) {
        List<InfoModel.Row> model = switch (activeTab) {
            case 1 -> InfoModel.body(player);
            case 2 -> InfoModel.mind(player);
            default -> InfoModel.aperture(player);
        };

        List<Row> rows = new ArrayList<>(model.size());
        for (InfoModel.Row row : model) {
            Row drawn = draw(row.indent(), row.entry());
            if (drawn != null) rows.add(drawn);
        }
        return rows;
    }

    //  ⚠ Exhaustive over a sealed Entry: a new row cannot be added to InfoModel without this failing to
    //  compile. That is what replaced the old "must not drift" comment in two files.
    private static @Nullable Row draw(int indent, InfoModel.Entry entry) {
        return switch (entry) {
            //  A section header takes the accent, which is what a null value means to render().
            case InfoModel.ApertureIndex e -> new Row(indent,
                    Component.translatable("guzhenren.command.info.aperture_index", e.number()), null);
            case InfoModel.Realm e -> new Row(indent, label("realm"), ModDisplayText.realm(e.aperture()));
            case InfoModel.Talent e -> new Row(indent, label("talent"), talent(e));
            case InfoModel.Essence e -> new Row(indent, label("essence"), Component.literal(
                    ModDisplayText.pool(e.aperture().currentEssence(), e.aperture().maxEssence())));

            //  The primary is read-only -- whatever Vital Gu sits in the slot. The secondary is the one
            //  row on this panel he may click. ⚠ The gray hint is the affordance; colour may not be.
            case InfoModel.PathChoice e -> e.primary()
                    ? new Row(indent, label("primary_path"), ModDisplayText.path(e.path()))
                    : new Row(indent, label("secondary_path"),
                            ModDisplayText.path(e.path()).append(detail(pickHint())), true);
            case InfoModel.ApertureLife e -> new Row(indent, label("state"), name(e.state().getTranslationKey()));

            case InfoModel.BodyLife e -> new Row(indent, label("state"), name(e.state().getTranslationKey()));
            case InfoModel.Form e -> new Row(indent, label("life_form"), name(e.form().getTranslationKey()));
            case InfoModel.Soul e -> new Row(indent, label("soul"),
                    Component.literal(ModDisplayText.pool(e.soul().currentSoul(), e.soul().maxSoul()))
                            .append(detail(name(e.soul().tier().getTranslationKey()))));
            case InfoModel.Lifespan e -> new Row(indent, label("lifespan"), ModDisplayText.lifespan(e.body()));
            case InfoModel.PathsHeader e -> new Row(indent, label("paths"), e.empty() ? none() : null);
            case InfoModel.PathRow e -> new Row(indent, name(e.path().getTranslationKey()), pathValue(e.entry()));
            case InfoModel.QiHeader e -> new Row(indent, label("qi"), qiValue(e));
            case InfoModel.QiRow e -> new Row(indent, name(e.type().getTranslationKey()),
                    Component.literal(Long.toString(e.mark())));
            case InfoModel.StrengthHeader e -> new Row(indent, label("strength"), e.empty() ? none() : null);
            case InfoModel.StrengthRow e -> new Row(indent, name(e.branch().getTranslationKey()), e.reading());

            case InfoModel.BrillianceRow e -> new Row(indent, label("brilliance"),
                    name(e.brilliance().getTranslationKey()).append(detail(Component.translatable(
                            "guzhenren.command.info.brilliance_rate", e.brilliance().getThoughtsPerSecond()))));
            case InfoModel.MindHeader ignored -> null;
            case InfoModel.MindRow e -> new Row(indent, name(e.type().getTranslationKey()),
                    Component.literal(ModDisplayText.pool(e.pool().current(), e.pool().max())));
        };
    }

    private static MutableComponent talent(InfoModel.Talent e) {
        MutableComponent talent = ModDisplayText.talent(e.aperture());
        if (e.awakened()) talent.append(detail(ModDisplayText.baseFraction(e.aperture().baseEssence())));
        return talent;
    }

    //  Marks always; specks only when he has some -- most mortals sit at one denomination, not both.
    private static MutableComponent pathValue(PathEntry entry) {
        MutableComponent value = Component.translatable("guzhenren.screen.path_value",
                name(entry.attainment().getTranslationKey()), entry.mark());
        if (entry.speck() > 0L) {
            value.append(Component.translatable("guzhenren.command.info.path_speck", entry.speck()));
        }
        return value;
    }

    //  Attainment plus total marks, or [NONE] while it is still none -- never a bare none.
    private static MutableComponent qiValue(InfoModel.QiHeader e) {
        MutableComponent value = e.attainment() == GuAttainment.NONE
                ? none()
                : name(e.attainment().getTranslationKey());
        if (e.total() > 0L) {
            value.append(Component.translatable("guzhenren.command.info.qi_total", e.total()));
        }
        return value;
    }

    private static MutableComponent name(String key) {return Component.translatable(key);}

    private static Component label(String name) {return Component.translatable("guzhenren.screen.label." + name);}
    private static MutableComponent none() {return Component.translatable("guzhenren.display.none");}

    //  Muted detail in [ ], gray -- the same key the command uses, so cmd / chat / screen read alike.
    private static Component detail(Component v) {
        return Component.translatable("guzhenren.command.info.detail", v).withStyle(ChatFormatting.DARK_GRAY);
    }

    //  One display line: label at the left, value in a fixed column so numbers line up across rows.
    //  ⚠ clickable is what makes a row an affordance; exactly one row (the secondary path) sets it.
    private record Row(int indent, Component label, @Nullable Component value, boolean clickable) {
        Row(int indent, Component label, @Nullable Component value) {this(indent, label, value, false);}
    }
}

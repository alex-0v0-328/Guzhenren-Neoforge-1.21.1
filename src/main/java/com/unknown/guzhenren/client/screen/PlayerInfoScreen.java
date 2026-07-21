package com.unknown.guzhenren.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.client.ModKeyMappings;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.network.OpenApertureStoragePayload;
import com.unknown.guzhenren.network.SetSecondaryPathPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  The G-key info panel: aperture / body / mind, plus a storage tab that opens a container instead.
//  Reads the synced attachments client-side, like the HUD. Layout notes: CLAUDE.md "Client".
//  TODO(refactor): this row logic mirrors CmdInfo -- extract a shared InfoModel when the view next grows.
public final class PlayerInfoScreen extends Screen {

    //  ⚠ No texture: the panel is drawn from fills, so it scales to any window. See CLAUDE.md "Client".
    private static final float SCREEN_FRACTION = 0.80F;
    private static final int PAD = 12;
    private static final int HEADER_H = 22;
    private static final int CONTENT_TOP = HEADER_H + 8;
    private static final int LINE_H = 12;
    private static final int INDENT = 10;

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
    //  it never says a number is good or bad. See CLAUDE.md "Color".
    private static final int[] ACCENT = {0xFF4FC3F7, 0xFFB388FF, 0xFFFFD54F, 0xFF81C784};

    private static final String[] TAB_KEYS = {
            "guzhenren.screen.tab.aperture",
            "guzhenren.screen.tab.body",
            "guzhenren.screen.tab.mind",
            "guzhenren.screen.tab.storage",
    };

    //  The storage tab does not render rows -- it opens a container instead.
    private static final int TAB_STORAGE = 3;

    //  The 辅修流派 picker: 30 paths plus a "clear" cell, laid out as a grid so it needs no scrolling.
    private static final int PICK_COLS = 4;
    private static final int PICK_CELL_W = 84;
    private static final int PICK_CELL_H = 14;
    private static final int PICK_PAD = 8;

    private int leftPos;
    private int topPos;
    private int panelW;
    private int panelH;
    private int activeTab = 0;

    //  Where the one clickable row landed this frame, or -1. ⚠ Set during render, read by mouseClicked
    //  -- the rows are rebuilt every frame, so there is no stable index to remember instead.
    private int clickableRowY = -1;
    private boolean picking;

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

        List<Row> rows = switch (activeTab) {
            case 1 -> bodyRows(player);
            case 2 -> mindRows(player);
            default -> apertureRows(player);
        };

        //  ⚠ Values are right-aligned against the tab rail, so numbers line up however long a label runs.
        int valueRight = tabLeft() - PAD;
        int y = topPos + CONTENT_TOP;
        clickableRowY = -1;
        for (Row row : rows) {
            if (mouseY >= y - 1 && mouseY < y + LINE_H - 1 && mouseX >= leftPos && mouseX < tabLeft()) {
                g.fill(leftPos + PAD - 2, y - 1, valueRight + 2, y + LINE_H - 1, ROW_HOVER);
            }
            if (row.clickable()) clickableRowY = y;
            //  A row with no value is a section header, so it takes the accent.
            int labelColor = row.value() == null ? accent : TEXT;
            g.drawString(font, row.label(), leftPos + PAD + row.indent(), y, labelColor, false);
            if (row.value() != null) {
                g.drawString(font, row.value(), valueRight - font.width(row.value()), y, TEXT, false);
            }
            y += LINE_H;
        }
        if (picking) renderPicker(g, mouseX, mouseY, accent);
    }

    //region 辅修流派 picker
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

    //  Aperture: realm, aptitude, essence (only once awakened), and its life state when not alive.
    private List<Row> apertureRows(LocalPlayer player) {
        ApertureData data = ApertureService.get(player);
        List<Row> rows = new ArrayList<>();
        if (data.count() <= 1) {
            apertureBlock(rows, data.primary(), data.isAwakened(), 0);
            return rows;
        }
        for (int i = 0; i < data.count(); i++) {
            rows.add(new Row(0, Component.translatable("guzhenren.command.info.aperture_index", i + 1), null));
            apertureBlock(rows, data.get(i), true, INDENT);
        }
        return rows;
    }

    private void apertureBlock(List<Row> rows, Aperture ap, boolean awakened, int indent) {
        rows.add(new Row(indent, label("realm"), ModDisplayText.realm(ap)));
        MutableComponent talent = ModDisplayText.talent(ap);
        if (awakened) talent.append(detail(ModDisplayText.baseFraction(ap.baseEssence())));
        rows.add(new Row(indent, label("talent"), talent));
        if (awakened) {
            rows.add(new Row(indent, label("essence"),
                    Component.literal(ModDisplayText.pool(ap.currentEssence(), ap.maxEssence()))));

            //  主修 is read-only -- it is whatever Vital Gu sits in the slot. 辅修 is the one row on this
            //  whole panel the player may click. ⚠ The gray hint is the affordance; colour may not be.
            rows.add(new Row(indent, label("primary_path"), ModDisplayText.path(ap.primaryPath())));
            rows.add(new Row(indent, label("secondary_path"),
                    ModDisplayText.path(ap.secondaryPath()).append(detail(pickHint())), true));
        }
        if (!ap.isAlive()) {
            rows.add(new Row(indent, label("state"), Component.translatable(ap.state().getTranslationKey())));
        }
    }

    //  Body: life state (only when not alive), life form, soul, lifespan, paths, the Qi Path, beast strengths.
    //  ⚠ Same rows in the same order as CmdInfo.body -- the two surfaces must not drift.
    private List<Row> bodyRows(LocalPlayer player) {
        BodyData body = BodyService.get(player);
        SoulData soul = SoulService.get(player);
        List<Row> rows = new ArrayList<>();

        if (body.lifeState() != LifeState.ALIVE) {
            rows.add(new Row(0, label("state"), Component.translatable(body.lifeState().getTranslationKey())));
        }
        rows.add(new Row(0, label("life_form"), Component.translatable(body.lifeForm().getTranslationKey())));
        rows.add(new Row(0, label("soul"), Component.literal(ModDisplayText.pool(soul.currentSoul(), soul.maxSoul()))
                .append(detail(Component.translatable(soul.tier().getTranslationKey())))));
        rows.add(new Row(0, label("lifespan"), ModDisplayText.lifespan(body)));

        //  Path attainment: every visible path except the Qi Path -- that one is its own section below.
        //  Empty reads inline on the header, not as a separate line.
        List<Map.Entry<GuPath, PathEntry>> paths = PathService.visibleEntries(player).entrySet().stream()
                .filter(e -> e.getKey() != GuPath.QI).toList();
        if (paths.isEmpty()) {
            rows.add(new Row(0, label("paths"), none()));
        } else {
            rows.add(new Row(0, label("paths"), null));
            for (Map.Entry<GuPath, PathEntry> e : paths) {
                PathEntry entry = e.getValue();
                MutableComponent value = Component.translatable("guzhenren.screen.path_value",
                        Component.translatable(entry.attainment().getTranslationKey()), entry.mark());
                if (entry.speck() > 0L) {
                    value.append(Component.translatable("guzhenren.command.info.path_speck", entry.speck()));
                }
                rows.add(new Row(INDENT, Component.translatable(e.getKey().getTranslationKey()), value));
            }
        }

        //  Qi Path: attainment + total marks on the header, or [NONE] while it is still none -- never a
        //  bare none. The per-type breakdown sits below.
        GuAttainment qiAttainment = PathService.attainment(player, GuPath.QI);
        MutableComponent qiValue = qiAttainment == GuAttainment.NONE ? none()
                : Component.translatable(qiAttainment.getTranslationKey());
        long qiTotal = PathService.mark(player, GuPath.QI);
        if (qiTotal > 0L) qiValue.append(Component.translatable("guzhenren.command.info.qi_total", qiTotal));
        rows.add(new Row(0, label("qi"), qiValue));
        for (QiType type : QiType.values()) {
            long mark = QiService.mark(player, type);
            if (mark <= 0L) continue;
            rows.add(new Row(INDENT, Component.translatable(type.getTranslationKey()),
                    Component.literal(Long.toString(mark))));
        }

        //  The Strength Path's two branches, one row each: how many beast strengths, and how many 斤 --
        //  never which. Empty reads [NONE] inline, as the path list does.
        //  ⚠ 力道 also stays in 流派造诣 above -- that row is its specks, these are the grades they bought.
        StrengthData strength = StrengthService.get(player);
        if (strength.isEmpty()) {
            rows.add(new Row(0, label("strength"), none()));
            return rows;
        }
        rows.add(new Row(0, label("strength"), null));
        if (strength.hasBranch(StrengthBranch.BEASTS)) {
            rows.add(new Row(INDENT, Component.translatable(StrengthBranch.BEASTS.getTranslationKey()),
                    ModDisplayText.boarStrength(strength.boarCount())));
        }
        //  One row per kind that has any. ⚠ A second kind would repeat the branch title -- revisit then.
        for (JunStrength kind : JunStrength.values()) {
            int count = strength.junCount(kind);
            if (count <= 0) continue;
            rows.add(new Row(INDENT, Component.translatable(StrengthBranch.HUMAN.getTranslationKey()),
                    ModDisplayText.junStrength(kind, count)));
        }
        return rows;
    }

    //  Mind: Brilliance with its regen rate, then the three cells -- the tab is the header already.
    private List<Row> mindRows(LocalPlayer player) {
        MindData mind = MindService.get(player);
        List<Row> rows = new ArrayList<>();
        rows.add(new Row(0, label("brilliance"), Component.translatable(mind.brilliance().getTranslationKey())
                .copy().append(detail(Component.translatable(
                        "guzhenren.command.info.brilliance_rate", mind.brilliance().getThoughtsPerSecond())))));
        for (WisdomType type : WisdomType.values()) {
            MindPool pool = mind.pool(type);
            rows.add(new Row(0, Component.translatable(type.getTranslationKey()),
                    Component.literal(ModDisplayText.pool(pool.current(), pool.max()))));
        }
        return rows;
    }

    private static Component label(String name) {return Component.translatable("guzhenren.screen.label." + name);}
    private static MutableComponent none() {return Component.translatable("guzhenren.display.none");}

    //  Muted detail in [ ], gray -- the same key the command uses, so cmd / chat / screen read alike.
    private static Component detail(Component v) {
        return Component.translatable("guzhenren.command.info.detail", v).withStyle(ChatFormatting.DARK_GRAY);
    }

    //  One display line: label at the left, value in a fixed column so numbers line up across rows.
    //  ⚠ clickable is what makes a row an affordance; exactly one row (辅修流派) sets it today.
    private record Row(int indent, Component label, @Nullable Component value, boolean clickable) {
        Row(int indent, Component label, @Nullable Component value) {this(indent, label, value, false);}
    }
}

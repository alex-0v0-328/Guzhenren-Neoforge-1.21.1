package com.unknown.guzhenren.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureNourishService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.client.ModKeyMappings;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.InfoModel;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.network.payload.ImpactApertureWallPayload;
import com.unknown.guzhenren.network.payload.NourishAperturePayload;
import com.unknown.guzhenren.network.payload.OpenApertureStoragePayload;
import com.unknown.guzhenren.network.payload.OpenRefinementPayload;
import com.unknown.guzhenren.network.payload.SetSecondaryPathPayload;
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * The B panel: every tab of what a player is, read straight off the synced attachments.
 *
 * <p>Extends {@link net.minecraft.client.gui.screens.Screen} (no menu behind it). Seven tabs: 空窍,
 * 肉身, 魂魄, 流派造诣, 脑海, 空窍存储, 炼蛊. The last two open a container via a client-intent payload
 * instead of drawing rows. Row content comes from {@link com.unknown.guzhenren.display.InfoModel},
 * shared with {@code /gzr info}, so the two surfaces cannot diverge.
 *
 * <p>⚠ A plain screen with no menu behind it: no container channel to send an intent over, which is
 * the whole reason a client-intent payload exists at all.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.display.InfoModel
 * @since 1.0.0
 */

public final class PlayerInfoScreen extends Screen {

    private static final float SCREEN_FRACTION = 0.80F;
    private static final int PAD = 12;
    private static final int HEADER_H = 22;
    private static final int CONTENT_TOP = HEADER_H + 8;
    private static final int LINE_H = 12;
    private static final int CONTENT_INSET_DIVISOR = 12;

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

    private static final int[] ACCENT =
            {0xFF4FC3F7, 0xFFFFAB91, 0xFFD388FF, 0xFFFF8A65, 0xFF4DD0E1, 0xFFFFD54F, 0xFF81C784};

    private static final String[] TAB_KEYS = {
            "guzhenren.screen.tab.aperture",
            "guzhenren.screen.tab.body",
            "guzhenren.screen.tab.soul",
            "guzhenren.screen.tab.path",
            "guzhenren.screen.tab.mind",
            "guzhenren.screen.tab.storage",
            "guzhenren.screen.tab.refinement",
    };

    private static final int BTN_H = 20;
    private static final int BTN_GAP = 4;
    private static final int BTN_IDLE = 0x33FFFFFF;
    private static final int BTN_HOVER = 0x66FFFFFF;
    private static final int BTN_DEAD = 0x14FFFFFF;
    private static final int BTN_PROGRESS = 0x804FC3F7;

    private static final String KEY_NOURISH = "guzhenren.screen.nourish";
    private static final String KEY_NOURISH_STOP = "guzhenren.screen.nourish_stop";
    private static final String KEY_NOURISH_SECOND = "guzhenren.screen.nourish_second";
    private static final String KEY_IMPACT = "guzhenren.screen.impact";

    private static final int TAB_APERTURE = 0;
    private static final int TAB_BODY = 1;
    private static final int TAB_SOUL = 2;
    private static final int TAB_PATH = 3;
    private static final int TAB_MIND = 4;
    private static final int TAB_STORAGE = 5;
    private static final int TAB_REFINEMENT = 6;

    private static final int PICK_COLS = 4;
    private static final int PICK_CELL_W = 84;
    private static final int PICK_CELL_H = 14;
    private static final int PICK_PAD = 8;

    private static final int SCROLL_W = 2;
    private static final int SCROLL_GAP = 5;

    private int leftPos;
    private int topPos;
    private int panelW;
    private int panelH;
    private int activeTab;

    private @Nullable Click hoverClick;
    private boolean picking;
    private int pickerAperture = ApertureData.PRIMARY;
    private int scrollRow;

    public PlayerInfoScreen() {super(Component.translatable("guzhenren.screen.info.title"));}
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

        g.drawString(font, Component.translatable(TAB_KEYS[activeTab]),
                leftPos + PAD, topPos + (HEADER_H - font.lineHeight) / 2, accent, false);
        g.fill(leftPos + PAD, topPos + HEADER_H, right - PAD, topPos + HEADER_H + 1, DIVIDER);

        renderTabs(g, mouseX, mouseY);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || activeTab == TAB_STORAGE) return;

        List<Row> rows = rows(player);

        int visible = visibleRows();
        int hidden = Math.max(0, rows.size() - visible);
        scrollRow = Mth.clamp(scrollRow, 0, hidden);

        int valueRight = valueRight();
        int rowLeft = contentLeft();
        int y = contentTop();
        hoverClick = null;
        for (int i = scrollRow; i < Math.min(rows.size(), scrollRow + visible); i++) {
            Row row = rows.get(i);
            if (mouseY >= y - 1 && mouseY < y + LINE_H - 1 && mouseX >= rowLeft - 2 && mouseX < valueRight + 2) {
                g.fill(rowLeft - 2, y - 1, valueRight + 2, y + LINE_H - 1, ROW_HOVER);
                if (row.click() != null) hoverClick = row.click();
            }
            int labelColor = row.value() == null ? accent : TEXT;
            g.drawString(font, row.label(), rowLeft + row.indent(), y, labelColor, false);
            if (row.value() != null) {
                g.drawString(font, row.value(), valueRight - font.width(row.value()), y, TEXT, false);
            }
            y += LINE_H;
        }
        if (hidden > 0) renderScrollBar(g, rows.size(), visible, accent);
        renderCultivation(g, mouseX, mouseY, accent);
        if (picking) renderPicker(g, mouseX, mouseY, accent);
    }
    //region 温养空窍 [nourish] and 冲刷窍壁 [flush] -- one button row per visible aperture, bottom of the panel
    private static int[] visibleCultivation(LocalPlayer player) {
        int count = ApertureService.get(player).count();
        int[] kept = new int[count];
        int visible = 0;
        for (int i = 0; i < count; i++) {
            if (!ApertureNourishService.atCeiling(player, i)) kept[visible++] = i;
        }
        return Arrays.copyOf(kept, visible);
    }
    private int cultivationCount() {
        LocalPlayer player = Minecraft.getInstance().player;
        return activeTab == TAB_APERTURE && player != null && ApertureService.isAwakened(player)
                ? visibleCultivation(player).length : 0;
    }
    private int buttonTop(int row) {
        return contentBottom() - cultivationCount() * (BTN_H + BTN_GAP) + BTN_GAP + row * (BTN_H + BTN_GAP);
    }
    private void renderCultivation(GuiGraphics g, int mouseX, int mouseY, int accent) {
        if (activeTab != TAB_APERTURE) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ApertureService.isAwakened(player)) return;
        int[] visible = visibleCultivation(player);

        for (int r = 0; r < visible.length; r++) {
            int aperture = visible[r];
            boolean paired = aperture == ApertureData.PRIMARY && ApertureNourishService.canImpact(player);
            boolean running = ApertureNourishService.isCultivating(player)
                    && ApertureNourishService.targetIndex(player) == aperture;

            int top = buttonTop(r);
            int x0 = contentLeft();
            int x3 = valueRight();
            if (paired) {
                int strike = !ApertureNourishService.canAffordImpact(player) ? BTN_DEAD
                        : inBox(mouseX, mouseY, x0, x3, r) ? BTN_HOVER : BTN_IDLE;
                g.fill(x0, top, x3, top + BTN_H, strike);
                g.renderOutline(x0, top, x3 - x0, BTN_H, accent);
                label(g, Component.translatable(KEY_IMPACT), x0, x3, top);
                continue;
            }

            int fill = running ? BTN_HOVER : ApertureNourishService.canNourish(player, aperture) ? BTN_IDLE : BTN_DEAD;

            g.fill(x0, top, x3, top + BTN_H, fill);
            if (running) {
                int done = x0 + Math.round((x3 - x0) * ApertureNourishService.fraction(player, aperture));
                g.fill(x0, top, done, top + BTN_H, BTN_PROGRESS);
            }
            g.renderOutline(x0, top, x3 - x0, BTN_H, running ? accent : BORDER);
            String nourish = aperture == ApertureData.PRIMARY ? KEY_NOURISH : KEY_NOURISH_SECOND;
            label(g, Component.translatable(running ? KEY_NOURISH_STOP : nourish), x0, x3, top);
        }
    }
    private void label(GuiGraphics g, Component text, int x0, int x1, int top) {
        g.drawString(font, text, x0 + (x1 - x0 - font.width(text)) / 2,
                top + (BTN_H - font.lineHeight) / 2, TEXT, false);
    }
    private boolean inBox(double mx, double my, int x0, int x1, int row) {
        return mx >= x0 && mx < x1 && my >= buttonTop(row) && my < buttonTop(row) + BTN_H;
    }
    private boolean clickCultivation(double mx, double my) {
        if (activeTab != TAB_APERTURE) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ApertureService.isAwakened(player)) return false;
        int[] visible = visibleCultivation(player);

        for (int r = 0; r < visible.length; r++) {
            int aperture = visible[r];
            if (aperture == ApertureData.PRIMARY && ApertureNourishService.canImpact(player)) {
                if (inBox(mx, my, contentLeft(), valueRight(), r)) {
                    PacketDistributor.sendToServer(ImpactApertureWallPayload.INSTANCE);
                    return true;
                }
                continue;
            }
            if (!inBox(mx, my, contentLeft(), valueRight(), r)) continue;

            if (ApertureNourishService.isCultivating(player)) {
                PacketDistributor.sendToServer(new NourishAperturePayload(NourishAperturePayload.Action.CANCEL,
                        ApertureNourishService.targetIndex(player)));
                return true;
            }
            if (!ApertureNourishService.canNourish(player, aperture)) return true;
            PacketDistributor.sendToServer(new NourishAperturePayload(NourishAperturePayload.Action.START, aperture));
            onClose();
            return true;
        }
        return false;
    }
    //endregion

    //region scrolling
    private int contentTop() {return topPos + CONTENT_TOP;}
    private int contentBottom() {return topPos + panelH - PAD;}
    private int contentLeft() {return edgeLeft() + inset();}
    private int valueRight() {return edgeRight() - inset();}
    private int inset() {return (edgeRight() - edgeLeft()) / CONTENT_INSET_DIVISOR;}
    private int edgeLeft() {return leftPos + PAD;}
    private int edgeRight() {return tabLeft() - PAD;}
    private int rowsBottom() {return contentBottom() - cultivationCount() * (BTN_H + BTN_GAP);}
    private int visibleRows() {return Math.max(0, (rowsBottom() - contentTop()) / LINE_H);}
    private void renderScrollBar(GuiGraphics g, int total, int visible, int accent) {
        int x0 = tabLeft() - SCROLL_GAP;
        int top = contentTop();
        int track = visible * LINE_H;

        g.fill(x0, top, x0 + SCROLL_W, top + track, DIVIDER);
        int thumb = Math.max(LINE_H, track * visible / total);
        int offset = (track - thumb) * scrollRow / Math.max(1, total - visible);
        g.fill(x0, top + offset, x0 + SCROLL_W, top + offset + thumb, accent);
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (picking) return true;
        if (dy == 0.0) return super.mouseScrolled(mx, my, dx, dy);

        scrollRow = Math.max(0, scrollRow - (int) Math.signum(dy));
        return true;
    }
    //endregion

    //region secondary-path picker
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
    private static int pickCount() {return GuPath.values().length + 1;}
    private static @Nullable GuPath pickPath(int i) {return i == 0 ? null : GuPath.values()[i - 1];}
    private static int pickRows() {return (pickCount() + PICK_COLS - 1) / PICK_COLS;}
    private int pickWidth() {return PICK_COLS * PICK_CELL_W + PICK_PAD * 2;}
    private int pickHeight() {return pickRows() * PICK_CELL_H + PICK_PAD * 2 + HEADER_H;}
    private int pickLeft() {return leftPos + (panelW - pickWidth()) / 2;}
    private int pickTop() {return topPos + (panelH - pickHeight()) / 2;}
    private static MutableComponent pickHint() {return Component.translatable("guzhenren.screen.pick.hint");}
    private void clickPicker(double mx, double my) {
        int x0 = pickLeft() + PICK_PAD;
        int y0 = pickTop() + PICK_PAD + HEADER_H;
        for (int i = 0; i < pickCount(); i++) {
            int cx = x0 + (i % PICK_COLS) * PICK_CELL_W;
            int cy = y0 + (i / PICK_COLS) * PICK_CELL_H;
            if (mx < cx || mx >= cx + PICK_CELL_W || my < cy || my >= cy + PICK_CELL_H) continue;

            PacketDistributor.sendToServer(new SetSecondaryPathPayload(pickerAperture, pickPath(i)));
            picking = false;
            return;
        }
        picking = false;
    }
    //endregion

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < TAB_KEYS.length; i++) {
            boolean active = i == activeTab;
            boolean live = tabLive(i);
            int x0 = tabLeft();
            int y0 = tabTop(i);
            boolean hover = live && !active && inTab(mouseX, mouseY, i);

            g.fill(x0, y0, x0 + TAB_W, y0 + TAB_H, active ? ACCENT[i] : TAB_IDLE);
            if (hover) g.fill(x0, y0, x0 + TAB_W, y0 + TAB_H, ROW_HOVER);
            if (active) g.fill(x0 - 2, y0, x0, y0 + TAB_H, ACCENT[i]);

            Component label = Component.translatable(TAB_KEYS[i]);
            int color = active ? 0xFF101010 : live ? TAB_TEXT_IDLE : TAB_TEXT_DEAD;
            g.drawString(font, label, x0 + (TAB_W - font.width(label)) / 2,
                    y0 + (TAB_H - font.lineHeight) / 2 + 1, color, false);
        }
    }
    private boolean tabLive(int tab) {
        if (tab != TAB_STORAGE && tab != TAB_REFINEMENT) return true;

        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ApertureService.isAwakened(player);
    }
    private int tabLeft() {return leftPos + panelW - TAB_W - PAD;}
    private int tabTop(int i) {return topPos + CONTENT_TOP + i * (TAB_H + TAB_GAP);}
    private boolean inTab(double mx, double my, int i) {
        return mx >= tabLeft() && mx < tabLeft() + TAB_W && my >= tabTop(i) && my < tabTop(i) + TAB_H;
    }
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (picking) {
            if (button == 0) clickPicker(mx, my);
            return true;
        }
        if (button == 0) {
            if (clickCultivation(mx, my)) return true;
            Click click = hoverClick;
            if (click != null) {
                if (click.picker()) {
                    pickerAperture = click.aperture();
                    picking = true;
                } else {
                    PacketDistributor.sendToServer(new OpenApertureStoragePayload(click.aperture()));
                }
                return true;
            }
            for (int i = 0; i < TAB_KEYS.length; i++) {
                if (!inTab(mx, my, i) || !tabLive(i)) continue;
                if (i == TAB_STORAGE) {
                    PacketDistributor.sendToServer(new OpenApertureStoragePayload(ApertureData.PRIMARY));
                } else if (i == TAB_REFINEMENT) {
                    PacketDistributor.sendToServer(OpenRefinementPayload.INSTANCE);
                } else {
                    activeTab = i;
                    scrollRow = 0;
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
    private List<Row> rows(LocalPlayer player) {
        List<InfoModel.Row> model = switch (activeTab) {
            case TAB_BODY -> InfoModel.body(player);
            case TAB_SOUL -> InfoModel.soul(player);
            case TAB_PATH -> InfoModel.pathAchieve(player);
            case TAB_MIND -> InfoModel.mind(player);
            default -> InfoModel.aperture(player);
        };

        List<Row> rows = new ArrayList<>(model.size());
        for (int i = 0; i < model.size(); i++) {
            InfoModel.Row row = model.get(i);
            if (activeTab == TAB_PATH && row.entry() instanceof InfoModel.PathRow first) {
                Row left = draw(row.indent(), first);
                if (left == null) continue;
                Row right = i + 1 < model.size()
                        && model.get(i + 1).entry() instanceof InfoModel.PathRow second
                        ? draw(model.get(i + 1).indent(), second) : null;
                if (right != null) {
                    rows.add(new Row(row.indent(), left.label(), right.label()));
                    i++;
                } else {
                    rows.add(left);
                }
                continue;
            }
            Row drawn = draw(row.indent(), row.entry());
            if (drawn != null) rows.add(drawn);
        }
        return rows;
    }
    private static @Nullable Row draw(int indent, InfoModel.Entry entry) {
        return switch (entry) {
            case InfoModel.ApertureIndex e -> new Row(indent, ModDisplayText.apertureName(e.number()), null,
                    new Click(false, e.number() - 1));
            case InfoModel.Blank ignored -> new Row(indent, Component.empty(), null);
            case InfoModel.Realm e -> new Row(indent, label("realm"), ModDisplayText.realmTitle(e.aperture()));
            case InfoModel.Status e -> new Row(indent, label("aperture_status"),
                    name(e.status().getTranslationKey()));
            case InfoModel.Talent e -> new Row(indent, label("talent"), talent(e));
            case InfoModel.Essence e -> new Row(indent, label("essence"), Component.literal(
                    ModDisplayText.pool(e.aperture().currentEssence(), e.aperture().maxEssence())));
            case InfoModel.Distilled e -> new Row(indent, label("distilled"), Component.literal(
                    ModDisplayText.pool(e.aperture().distilledEssence(), e.aperture().maxEssence())));
            case InfoModel.Pressure e -> new Row(indent,
                    Component.translatable("guzhenren.screen.label.aperture_pressure"), Component.literal(
                    e.aperture().pressure() + "%"));

            case InfoModel.PathChoice e -> e.primary()
                    ? new Row(indent, label("primary_path"), ModDisplayText.path(e.path()))
                    : new Row(indent, label("secondary_path"),
                    detail(pickHint()).copy().append(detail(ModDisplayText.path(e.path()))),
                    new Click(true, e.aperture()));

            case InfoModel.PhysiqueRow e -> new Row(indent, label("physique"),
                    ModDisplayText.physique(e.physique(), e.extremePhysique()));
            case InfoModel.RaceRow e -> new Row(indent, label("race"), name(e.race().getTranslationKey()));
            case InfoModel.Soul e -> new Row(indent, label("soul"),
                    Component.literal(ModDisplayText.pool(e.soul().currentSoul(), e.soul().maxSoul()))
                            .append(detail(name(e.soul().tier().getTranslationKey()))));
            case InfoModel.Lifespan e -> new Row(indent, label("lifespan"),
                    ModDisplayText.lifespan(e.lifespan(), e.age()));
            case InfoModel.PathsHeader e -> new Row(indent, label("paths"), e.empty() ? none() : null);
            case InfoModel.PathRow e ->
                    new Row(indent, ModDisplayText.pathLine(e.path(), e.entry()), Component.empty());
            case InfoModel.QiPathAchieveHeader ignored -> new Row(indent, label("qi_path_achieve"), null);
            case InfoModel.QiKindRow e -> new Row(indent, name(e.kind().getTranslationKey()),
                    Component.literal(String.valueOf(e.amount())));
            case InfoModel.TimePathAchieveHeader ignored -> new Row(indent, label("time_path_achieve"), null);
            case InfoModel.TimeRateUpRow e -> new Row(indent, label("time_rate_up"),
                    ModDisplayText.timeRateUp(e.rate()));
            case InfoModel.StrengthPathAchieveHeader ignored -> new Row(indent, label("strength_path_achieve"), null);
            case InfoModel.StrengthPathBranchRow e -> new Row(indent,
                    ModDisplayText.strengthLabel(name(e.branch().getTranslationKey()), e.totalJin()), e.reading());
            case InfoModel.CapacityRow e -> new Row(indent,
                    Component.translatable("guzhenren.screen.label.body_capacity"),
                    Component.translatable("guzhenren.screen.capacity", e.usable(), e.total()));
            case InfoModel.AttackRow e -> new Row(indent, label("attack"),
                    Component.literal(ModDisplayText.attackBonus(e.bonus())));
            case InfoModel.WisdomPathAchieveHeader ignored -> new Row(indent, label("wisdom_path_achieve"), null);
            case InfoModel.ThoughtTagRow e -> new Row(indent, name(e.tag().getTranslationKey()),
                    Component.literal(String.valueOf(e.amount())));

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
    private static MutableComponent name(String key) {return Component.translatable(key);}
    private static Component label(String name) {return Component.translatable("guzhenren.screen.label." + name);}
    private static MutableComponent none() {return Component.translatable("guzhenren.display.none");}
    private static Component detail(Component v) {
        return Component.translatable("guzhenren.command.info.detail", v).withStyle(ChatFormatting.DARK_GRAY);
    }
    private record Row(int indent, Component label, @Nullable Component value, @Nullable Click click) {
        Row(int indent, Component label, @Nullable Component value) {this(indent, label, value, null);}
    }
    private record Click(boolean picker, int aperture) {}
}

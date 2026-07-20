package com.unknown.guzhenren.client.screen;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  The G-key info panel: aperture / body / mind across three tabs, one domain each. Reads the synced
//  attachments client-side, exactly like the HUD -- no server round-trip. Layout notes: CLAUDE.md "Info panel".
//  TODO(refactor): this row logic mirrors CmdInfo -- extract a shared InfoModel when the view next grows.
public final class PlayerInfoScreen extends Screen {

    //  ⚠ No texture: the panel is drawn from fills, so it scales to any window. See CLAUDE.md "Client".
    private static final float SCREEN_FRACTION = 0.80F;
    private static final int PAD = 12;
    private static final int CONTENT_TOP = 14;
    private static final int LINE_H = 11;
    private static final int VALUE_X = 84;
    private static final int INDENT = 10;

    //  The tab rail down the right edge.
    private static final int TAB_W = 72;
    private static final int TAB_H = 20;
    private static final int TAB_GAP = 4;

    //  75% black panel, white text -- the whole look is these two.
    private static final int PANEL_FILL = 0xBF000000;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TAB_FILL_ACTIVE = 0x99FFFFFF;
    private static final int TAB_FILL_IDLE = 0x33FFFFFF;
    private static final int TAB_TEXT_ACTIVE = 0xFF101010;
    private static final int TAB_TEXT_IDLE = 0xFFDDDDDD;

    private static final String[] TAB_KEYS = {
            "guzhenren.screen.tab.aperture",
            "guzhenren.screen.tab.body",
            "guzhenren.screen.tab.mind",
    };

    private int leftPos;
    private int topPos;
    private int panelW;
    private int panelH;
    private int activeTab = 0;

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
        g.fill(leftPos, topPos, leftPos + panelW, topPos + panelH, PANEL_FILL);
        renderTabs(g);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        List<Row> rows = switch (activeTab) {
            case 1 -> bodyRows(player);
            case 2 -> mindRows(player);
            default -> apertureRows(player);
        };
        int y = topPos + CONTENT_TOP;
        for (Row row : rows) {
            int labelX = leftPos + PAD + row.indent();
            g.drawString(font, row.label(), labelX, y, TEXT, false);
            if (row.value() != null) {
                //  Fixed column for aligned numbers; a longer label (English) pushes the value past it.
                int valueX = Math.max(leftPos + VALUE_X, labelX + font.width(row.label()) + 4);
                g.drawString(font, row.value(), valueX, y, TEXT, false);
            }
            y += LINE_H;
        }
    }

    //  A rail of buttons down the right edge: filled while active, faint while idle.
    private void renderTabs(GuiGraphics g) {
        for (int i = 0; i < TAB_KEYS.length; i++) {
            boolean active = i == activeTab;
            int x0 = tabLeft();
            int y0 = tabTop(i);
            g.fill(x0, y0, x0 + TAB_W, y0 + TAB_H, active ? TAB_FILL_ACTIVE : TAB_FILL_IDLE);
            Component label = Component.translatable(TAB_KEYS[i]);
            g.drawString(font, label, x0 + (TAB_W - font.width(label)) / 2, y0 + (TAB_H - font.lineHeight) / 2 + 1,
                    active ? TAB_TEXT_ACTIVE : TAB_TEXT_IDLE, false);
        }
    }

    private int tabLeft() {return leftPos + panelW - TAB_W - PAD;}
    private int tabTop(int i) {return topPos + PAD + i * (TAB_H + TAB_GAP);}

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && mx >= tabLeft() && mx < tabLeft() + TAB_W) {
            for (int i = 0; i < TAB_KEYS.length; i++) {
                if (my >= tabTop(i) && my < tabTop(i) + TAB_H) {
                    activeTab = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    //  The open key toggles the panel shut too, not only Escape.
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
    private record Row(int indent, Component label, @Nullable Component value) {
    }
}

package com.unknown.guzhenren.client.screen;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.client.ModKeyMappings;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  The G-key info panel: 空窍 / 肉身 / 脑海 across three tabs, one domain each. Reads the synced
//  attachments client-side, exactly like the HUD -- no server round-trip. Layout notes: CLAUDE.md "Info panel".
public final class PlayerInfoScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "textures/gui/player_info.png");

    //  The panel is the vanilla inventory's size, so it centers the same way.
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;
    private static final int PAD = 8;
    private static final int TAB_Y = 4;
    private static final int TAB_H = 14;
    private static final int CONTENT_TOP = 24;
    private static final int LINE_H = 11;
    private static final int VALUE_X = 36;
    private static final int INDENT = 8;

    //  Dark text on the light panel, no shadow -- the vanilla container-label look.
    private static final int TEXT = 0xFF404040;
    private static final int TAB_BORDER = 0xFF303030;
    private static final int TAB_FILL_ACTIVE = 0xFF6A6A6A;
    private static final int TAB_FILL_IDLE = 0xFFBFBFBF;
    private static final int TAB_TEXT_ACTIVE = 0xFFFFFFFF;
    private static final int TAB_TEXT_IDLE = 0xFF404040;

    private static final String[] TAB_KEYS = {
            "guzhenren.screen.tab.aperture",
            "guzhenren.screen.tab.body",
            "guzhenren.screen.tab.mind",
    };

    private int leftPos;
    private int topPos;
    private int activeTab = 0;

    public PlayerInfoScreen() {super(Component.translatable("guzhenren.screen.info.title"));}

    @Override
    protected void init() {
        leftPos = (width - PANEL_W) / 2;
        topPos = (height - PANEL_H) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
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

    //  Button-like tabs so it reads as clickable: raised light box idle, pressed dark box active.
    private void renderTabs(GuiGraphics g) {
        int third = PANEL_W / 3;
        for (int i = 0; i < TAB_KEYS.length; i++) {
            boolean active = i == activeTab;
            int x0 = leftPos + third * i + 2;
            int x1 = leftPos + third * (i + 1) - 2;
            int y0 = topPos + TAB_Y;
            int y1 = y0 + TAB_H;
            g.fill(x0, y0, x1, y1, TAB_BORDER);
            g.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, active ? TAB_FILL_ACTIVE : TAB_FILL_IDLE);
            Component label = Component.translatable(TAB_KEYS[i]);
            g.drawString(font, label, (x0 + x1 - font.width(label)) / 2, y0 + 3,
                    active ? TAB_TEXT_ACTIVE : TAB_TEXT_IDLE, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int y0 = topPos + TAB_Y;
        if (button == 0 && my >= y0 && my < y0 + TAB_H) {
            int rel = (int) mx - leftPos;
            if (rel >= 0 && rel < PANEL_W) {
                activeTab = Math.min(TAB_KEYS.length - 1, rel / (PANEL_W / 3));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    //  The open key toggles the panel shut too, not only Escape.
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyMappings.OPEN_INFO.matches(keyCode, scanCode)) {onClose(); return true;}
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {return false;}

    //  空窍: realm, aptitude, essence (only once awakened), and the aperture's 生死 when not alive.
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

    //  肉身: 生死僵 (only when not 生), 凡/仙, soul, lifespan, then paths, then the qi path on its own.
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

        //  流派造诣: every visible path except 气道 -- the qi path is its own section below. Empty reads
        //  inline on the header (流派造诣  [无]), not a separate line.
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

        //  气道造诣: 造诣 + total 道痕 on the header, or [无] while it is still 无 -- never a bare 无, so an
        //  empty qi path reads 气道造诣  [无]. The 天/地/人/自然 breakdown (marks) sits below.
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
        return rows;
    }

    //  脑海: 才情 with its regen rate, then 念 / 意 / 情 -- the tab already says 脑海, so no header.
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
    private record Row(int indent, Component label, @Nullable Component value) {}
}

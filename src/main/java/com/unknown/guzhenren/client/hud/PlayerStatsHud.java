package com.unknown.guzhenren.client.hud;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.display.ModDisplayText;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

//  The player-stats HUD, top-left corner. Layout and rationale: CLAUDE.md "HUD".
//  Reads the same attachments the server holds; the primary aperture only, Mind Ocean deliberately absent.
public final class PlayerStatsHud implements LayeredDraw.Layer {

    public static final PlayerStatsHud INSTANCE = new PlayerStatsHud();

    private PlayerStatsHud() {}

    private static final int LEFT = 8;
    private static final int TOP = 8;

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 11;
    private static final int TEXT_HEIGHT = 9;

    //  GROUP_GAP sets the lifespan line apart: it is not a spendable pool.
    private static final int ROW_GAP = 2;
    private static final int GROUP_GAP = 7;

    //  Fixed hues, NOT EssenceColor's per-rank palette --  CLAUDE.md "HUD".
    private static final int ESSENCE_FILL = 0xFF4FC3F7;
    private static final int SOUL_FILL = 0xFFB388FF;

    //  ⚠ Deliberately the SAME hue family as essence, several shades down: distilled essence [精炼真元]
    //  is that same essence concentrated, and the bar right below it should read as a relative, not a
    //  stranger. Identity, not status -- see the note above.
    private static final int DISTILLED_FILL = 0xFF1565C0;
    private static final int BAR_TRACK = 0xB0202020;
    private static final int BAR_BORDER = 0xC0000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;

        //  A spectator has no cultivation, and F3 owns this same corner.
        if (player.isSpectator() || minecraft.getDebugOverlay().showDebugScreen()) return;

        Font font = minecraft.font;
        Aperture aperture = ApertureService.aperture(player);
        SoulData soul = SoulService.get(player);
        BodyData body = BodyService.get(player);

        int y = TOP;
        line(graphics, font, y, ModDisplayText.realmAndTalent(aperture));
        y += TEXT_HEIGHT + ROW_GAP;

        //  Hidden until the aperture opens -- its appearing is the feedback that awakening worked.
        if (ApertureService.isAwakened(player)) {
            bar(graphics, font, y, aperture.currentEssence(), aperture.maxEssence(), ESSENCE_FILL);
            y += BAR_HEIGHT + ROW_GAP;

            //  ⚠ Only while it holds something. Outside a Liquor Worm this pool is 0 for a cultivator's
            //  whole life, and a permanent empty bar would push every row below it down for nothing.
            if (aperture.distilledEssence() > 0L) {
                bar(graphics, font, y, aperture.distilledEssence(), aperture.maxEssence(), DISTILLED_FILL);
                y += BAR_HEIGHT + ROW_GAP;
            }
        }

        bar(graphics, font, y, soul.currentSoul(), soul.maxSoul(), SOUL_FILL);
        y += BAR_HEIGHT + GROUP_GAP;

        line(graphics, font, y, Component.translatable("guzhenren.hud.lifespan", ModDisplayText.lifespan(body)));
    }

    private static void line(GuiGraphics graphics, Font font, int y, Component text) {
        graphics.drawString(font, text, LEFT, y, TEXT_COLOR, true);
    }

    //  Raw value over the fill, not a percentage. A max of 0 is legal and reads 0/0.
    private static void bar(GuiGraphics graphics, Font font, int y, long current, long max, int fill) {
        int right = LEFT + BAR_WIDTH;
        graphics.fill(LEFT, y, right, y + BAR_HEIGHT, BAR_BORDER);
        graphics.fill(LEFT + 1, y + 1, right - 1, y + BAR_HEIGHT - 1, BAR_TRACK);

        int track = BAR_WIDTH - 2;
        int filled = max <= 0L ? 0 : Math.clamp(Math.round(track * (current / (double) max)), 0, track);
        if (filled > 0) {
            graphics.fill(LEFT + 1, y + 1, LEFT + 1 + filled, y + BAR_HEIGHT - 1, fill);
        }

        String text = ModDisplayText.pool(current, max);
        graphics.drawString(font, text, LEFT + (BAR_WIDTH - font.width(text)) / 2, y + 2, TEXT_COLOR, true);
    }
}

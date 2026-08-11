package com.unknown.guzhenren.client.hud;

import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The bar drawn over the hotbar while the aperture is being nourished [温养空窍].
 *
 * <p>⚠ It is a separate layer from the charge bar on purpose: that one reads only the held item, and
 * grafting a player attachment into it would take away the property its own header states.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class NourishHud implements LayeredDraw.Layer {

    public static final NourishHud INSTANCE = new NourishHud();

    private NourishHud() {}

    private static final String CAPTION = "guzhenren.hud.nourishing";
    private static final String STARVING = "guzhenren.hud.nourish_starving";

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXT_GAP = 3;
    private static final int NAME_GAP = 4;
    private static final int MIN_SHIFT = 59;
    private static final int CREATIVE_LIFT = 14;

    private static final int TRACK = 0xB0202020;
    private static final int BORDER = 0xC0000000;
    private static final int FILL = 0xFF4FC3F7;
    private static final int FILL_STARVING = 0xFFE57373;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        if (!NourishService.isCultivating(player)) return;

        boolean starving = NourishService.get(player).isStarving();
        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = barTop(minecraft);

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, TRACK);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * NourishService.fraction(player)), y + BAR_HEIGHT,
                starving ? FILL_STARVING : FILL);

        Font font = minecraft.font;
        Component label = Component.translatable(starving ? STARVING : CAPTION,
                NourishService.get(player).progress());
        graphics.drawString(font, label, x + (BAR_WIDTH - font.width(label)) / 2,
                y - TEXT_GAP - font.lineHeight, TEXT_COLOR, true);
    }

    private static int barTop(Minecraft minecraft) {
        Gui gui = minecraft.gui;
        int shift = Math.max(Math.max(gui.leftHeight, gui.rightHeight), MIN_SHIFT);
        int baseline = minecraft.getWindow().getGuiScaledHeight() - shift;
        if (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer()) baseline += CREATIVE_LIFT;
        return baseline - NAME_GAP - BAR_HEIGHT;
    }
}

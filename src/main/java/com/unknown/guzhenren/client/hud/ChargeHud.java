package com.unknown.guzhenren.client.hud;

import com.unknown.guzhenren.item.GuItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ChargeHud implements LayeredDraw.Layer {

    public static final ChargeHud INSTANCE = new ChargeHud();

    private ChargeHud() {}


    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXT_GAP = 3;

    private static final int NAME_GAP = 4;

    private static final int MIN_SHIFT = 59;
    private static final int CREATIVE_LIFT = 14;

    private static final int TRACK = 0xB0202020;
    private static final int BORDER = 0xC0000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        if (!player.isUsingItem()) return;

        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof GuItem gu)) return;

        int total = stack.getUseDuration(player);
        if (total <= 0) return;

        int remaining = player.getUseItemRemainingTicks();
        Float own = gu.chargeFraction(stack, remaining);
        float progress = own != null ? own : 1.0F - remaining / (float) total;

        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = barTop(minecraft);

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, TRACK);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * Math.clamp(progress, 0.0F, 1.0F)), y + BAR_HEIGHT,
                gu.chargeColor(stack, remaining));

        Component label = gu.chargeCaption(stack, remaining);
        if (label == null) return;

        Font font = minecraft.font;
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

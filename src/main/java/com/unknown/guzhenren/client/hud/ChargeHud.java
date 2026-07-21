package com.unknown.guzhenren.client.hud;

import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.item.RefinedGuState;
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

//  The bar a charged Gu use draws over the hotbar, with the running total above it. It rides vanilla's
//  own held-item-name baseline, so armour and a mount's health push it up instead of hiding it.
//  ⚠ Purely client-side and purely derived: the hold lives in vanilla's own useItemRemainingTicks, and
//  the totals ride the stack's synced component. Nothing here is state.  CLAUDE.md "HUD".
public final class ChargeHud implements LayeredDraw.Layer {

    public static final ChargeHud INSTANCE = new ChargeHud();

    private ChargeHud() {}

    private static final String LABEL_REFINING = "guzhenren.hud.refining";
    private static final String LABEL_USING = "guzhenren.hud.using";

    //  Hotbar is 182 wide, so the bar lines up with it. The HEIGHT is not fixed -- see barTop.
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXT_GAP = 3;

    //  Clearance over the held-item name, which sits at vanilla's own baseline.
    private static final int NAME_GAP = 4;

    //  Vanilla's floor for that baseline, and the lift it applies where nothing can hurt you.
    private static final int MIN_SHIFT = 59;
    private static final int CREATIVE_LIFT = 14;

    //  Essence blue, the same hue the HUD's essence bar uses -- this bar is spending exactly that.
    private static final int FILL = 0xFF4FC3F7;
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
        if (!(stack.getItem() instanceof RefinableGuItem gu)) return;

        int total = stack.getUseDuration(player);
        if (total <= 0) return;

        //  getUseItemRemainingTicks counts DOWN, so the fraction is what has already elapsed.
        float progress = 1.0F - player.getUseItemRemainingTicks() / (float) total;

        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = barTop(minecraft);

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, TRACK);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * Math.clamp(progress, 0.0F, 1.0F)), y + BAR_HEIGHT, FILL);

        Font font = minecraft.font;
        Component label = caption(gu, stack);
        graphics.drawString(font, label, x + (BAR_WIDTH - font.width(label)) / 2,
                y - TEXT_GAP - font.lineHeight, TEXT_COLOR, true);
    }

    //  Sits just above the held-item name, on vanilla's own baseline -- so armour, absorption and a
    //  mount's health push it up exactly as they push that name up.
    //  ⚠ This is why the layer is registered above AIR_LEVEL and not HOTBAR: leftHeight/rightHeight are
    //  reset at the top of every frame and only reach their final value once the status stack has drawn.
    private static int barTop(Minecraft minecraft) {
        Gui gui = minecraft.gui;
        int shift = Math.max(Math.max(gui.leftHeight, gui.rightHeight), MIN_SHIFT);
        int baseline = minecraft.getWindow().getGuiScaledHeight() - shift;
        //  Vanilla lifts the same line in creative, where there is no health row under it.
        if (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer()) baseline += CREATIVE_LIFT;
        return baseline - NAME_GAP - BAR_HEIGHT;
    }

    //  炼化中 320 / 640 while wild, 使用中 12 / 36 once it answers to him -- the same two numbers the
    //  tooltip carries, so the bar never says something the item does not.
    private static Component caption(RefinableGuItem gu, ItemStack stack) {
        RefinedGuState state = RefinableGuItem.state(stack);
        return gu.refined(stack)
                ? Component.translatable(LABEL_USING, state.useCount(), gu.usesPerGrant())
                : Component.translatable(LABEL_REFINING, state.refineProgress(), gu.refineCost());
    }
}

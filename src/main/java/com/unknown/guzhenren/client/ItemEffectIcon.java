package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Draws an effect with the icon of the item that grants it, so the two always read as the same thing.
 *
 * <p>⚠ It takes the item's registration id, not a mob_effect texture. An effect wearing its Gu's face
 * needs no second drawing, and a missing one here would be a checkerboard rather than a fallback.
 *
 * @author Alex
 * @since 1.0.0
 * @see GradedEffectIcon
 */
public record ItemEffectIcon(String item) implements IClientMobEffectExtensions {

    private static final int ICON_SIZE = 16;
    private static final int CENTRE_IN_SLOT = 1;
    private static final int HUD_SLOT_X = 3;
    private static final int HUD_SLOT_Y = 3;
    private static final int INVENTORY_SLOT_Y = 7;

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics,
                                 int x, int y, float z, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        draw(graphics, x + HUD_SLOT_X + CENTRE_IN_SLOT, y + HUD_SLOT_Y + CENTRE_IN_SLOT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
                                       GuiGraphics graphics, int x, int y, int blitOffset) {
        draw(graphics, x + CENTRE_IN_SLOT, y + INVENTORY_SLOT_Y + CENTRE_IN_SLOT);
        return true;
    }

    private void draw(GuiGraphics graphics, int x, int y) {
        ResourceLocation texture =
                ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "textures/item/" + item + ".png");
        graphics.blit(texture, x, y, 0, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}

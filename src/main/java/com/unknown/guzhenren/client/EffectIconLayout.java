package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Shared layout for an effect icon drawn from a texture: slot centring, the HUD/inventory render hooks,
 * and the one abstract seam -- which texture this instance wears.
 *
 * @author Alex
 * @since 1.0.0
 */
interface EffectIconLayout extends IClientMobEffectExtensions {

    int ICON_SIZE = 16;
    int CENTRE_IN_SLOT = 1;
    int HUD_SLOT_X = 3;
    int HUD_SLOT_Y = 3;
    int INVENTORY_SLOT_Y = 7;

    String textureFor(MobEffectInstance instance);

    private void draw(GuiGraphics g, MobEffectInstance instance, int x, int y) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID,
                "textures/" + textureFor(instance) + ".png");
        g.blit(texture, x, y, 0, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    @Override
    default boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics,
                                  int x, int y, float z, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        draw(graphics, instance, x + HUD_SLOT_X + CENTRE_IN_SLOT, y + HUD_SLOT_Y + CENTRE_IN_SLOT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    @Override
    default boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
                                        GuiGraphics graphics, int x, int y, int blitOffset) {
        draw(graphics, instance, x + CENTRE_IN_SLOT, y + INVENTORY_SLOT_Y + CENTRE_IN_SLOT);
        return true;
    }
}

package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Draws a graded effect's icon from its amplifier, so one effect can wear a different face per grade.
 *
 * <p>⚠ This exists so a family of grades stays a single effect. Splitting it into one effect per
 * grade would let a player hold two grades of the same thing at the same time.
 *
 * <p>The directory selects mob_effect textures or the Gu's own item texture; see the two factories.
 *
 * @author Alex
 * @since 1.0.0
 */
public record GradedEffectIcon(String name, String directory, int lowestRank, int highestRank) implements IClientMobEffectExtensions {

    public static GradedEffectIcon mobEffect(String name, int lowestRank, int highestRank) {
        return new GradedEffectIcon(name, "mob_effect", lowestRank, highestRank);
    }
    public static GradedEffectIcon item(String name, int lowestRank, int highestRank) {
        return new GradedEffectIcon(name, "item", lowestRank, highestRank);
    }

    private static final int ICON_SIZE = 16;
    private static final int CENTRE_IN_SLOT = 1;
    private static final int HUD_SLOT_X = 3;
    private static final int HUD_SLOT_Y = 3;
    private static final int INVENTORY_SLOT_Y = 7;

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics,
                                 int x, int y, float z, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        draw(graphics, instance, x + HUD_SLOT_X + CENTRE_IN_SLOT, y + HUD_SLOT_Y + CENTRE_IN_SLOT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
                                       GuiGraphics graphics, int x, int y, int blitOffset) {
        draw(graphics, instance, x + CENTRE_IN_SLOT, y + INVENTORY_SLOT_Y + CENTRE_IN_SLOT);
        return true;
    }

    private void draw(GuiGraphics graphics, MobEffectInstance instance, int x, int y) {
        int rank = Mth.clamp(instance.getAmplifier() + 1, lowestRank, highestRank);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID,
                "textures/" + directory + "/" + name + "_" + rank + ".png");
        graphics.blit(texture, x, y, 0, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}

package com.unknown.guzhenren.client.hud;

import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
public final class NourishHud extends HotbarHud {

    public static final NourishHud INSTANCE = new NourishHud();

    private NourishHud() {}

    private static final String CAPTION = "guzhenren.hud.nourishing";
    private static final String STARVING = "guzhenren.hud.nourish_starving";

    private static final int FILL = 0xFF4FC3F7;
    private static final int FILL_STARVING = 0xFFE57373;

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        if (!NourishService.isCultivating(player)) return;

        NourishData nourish = NourishService.get(player);
        boolean starving = nourish.isStarving();
        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = barTop(minecraft);

        drawBar(graphics, x, y, NourishService.fraction(player), starving ? FILL_STARVING : FILL);

        drawLabel(graphics, minecraft, x, y,
                Component.translatable(starving ? STARVING : CAPTION, nourish.progress()));
    }
}

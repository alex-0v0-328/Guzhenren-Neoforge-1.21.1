package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * The client half of the cultivation stance [温养空窍]: it holds the player still and pulls the view in.
 *
 * <p>Annotated {@code @EventBusSubscriber(Dist.CLIENT)}. On {@code MovementInputUpdateEvent} it zeroes
 * every input axis while {@link com.unknown.guzhenren.attachment.service.aperture.NourishService#isCultivating}
 * is true; on {@code ComputeFovModifierEvent} it sets a multiplier so the view pulls in to 100.
 *
 * <p>☠ Standing still can only be enforced on the CLIENT. A server-side stop is undone on the very
 * next tick by whatever key is held -- client input is the only authority that can keep it still.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

@EventBusSubscriber(modid = Guzhenren.MOD_ID, value = Dist.CLIENT)
public final class NourishClientEvents {

    private NourishClientEvents() {}

    public static final float CULTIVATION_FOV = 100.0F;

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!NourishService.isCultivating(event.getEntity())) return;

        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        if (!NourishService.isCultivating(event.getPlayer())) return;

        int own = Minecraft.getInstance().options.fov().get();
        if (own > 0) event.setNewFovModifier(CULTIVATION_FOV / own);
    }
}

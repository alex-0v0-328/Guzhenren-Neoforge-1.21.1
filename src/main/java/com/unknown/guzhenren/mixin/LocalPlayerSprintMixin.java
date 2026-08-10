package com.unknown.guzhenren.mixin;

import com.unknown.guzhenren.attachment.service.body.StaminaService;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSprintMixin {

    @Inject(method = "hasEnoughFoodToStartSprinting", at = @At("HEAD"), cancellable = true)
    private void guzhenren$staminaGatesSprinting(CallbackInfoReturnable<Boolean> callback) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        boolean allowed = player.isSprinting()
                ? StaminaService.canKeepSprinting(player)
                : StaminaService.canResumeSprinting(player);

        if (!allowed) callback.setReturnValue(false);
    }
}

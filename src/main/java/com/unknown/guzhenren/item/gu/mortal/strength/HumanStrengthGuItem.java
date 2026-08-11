package com.unknown.guzhenren.item.gu.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The shared class behind the human-strength ladder; the rank and the kind come from registration.
 *
 * <p>⚠ What it grants rides the player rather than the stack, so a holder who has already maxed out
 * can hand the Gu to someone else and it goes on working for them.
 *
 * @author Alex
 * @since 1.0.0
 */
public class HumanStrengthGuItem extends TendedGuItem {

    private static final String FAILED_LAYERS_FULL = "guzhenren.item.failed.human_strength_full";

    private static final int LAYERS_PER_GRANT = 1;

    private final HumanStrength kind;

    public HumanStrengthGuItem(Properties properties, HumanStrength kind, GuSpec spec) {
        super(properties, spec);
        this.kind = kind;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return StrengthService.humanStrength(player, kind) >= kind.getMaxLayers()
                ? new Refusal(FAILED_LAYERS_FULL, Component.literal(String.valueOf(kind.getMaxLayers())))
                : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        StrengthService.addHumanStrength(player, kind, LAYERS_PER_GRANT);
    }
}

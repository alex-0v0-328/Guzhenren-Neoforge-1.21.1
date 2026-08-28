package com.unknown.guzhenren.item.gu.mortal.strength;

import com.unknown.guzhenren.attachment.service.path.PathStrengthService;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The shared class behind the human-strength [人力] ladder; the rank [转数] and the kind come from registration.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.TendedGuItem}. Four rungs register against this one
 * class (斤 / 十斤 / 钧 / 十钧). The gate refuses a holder who has maxed out that kind; the apply
 * delegates to {@link com.unknown.guzhenren.attachment.service.path.PathStrengthService#addHumanStrength}.
 * One layer per grant is fixed by {@code LAYERS_PER_GRANT}.
 *
 * <p>⚠ What it grants rides the player rather than the stack, so a holder who has already maxed out
 * can hand the Gu to someone else, and it goes on working for them.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.TendedGuItem
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
        return PathStrengthService.humanStrength(player, kind) >= kind.getMaxLayers()
                ? new Refusal(FAILED_LAYERS_FULL, Component.literal(String.valueOf(kind.getMaxLayers())))
                : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        PathStrengthService.addHumanStrength(player, kind, LAYERS_PER_GRANT);
    }
}

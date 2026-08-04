package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.item.GuSpec;
import com.unknown.guzhenren.item.TendedGuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BeastStrengthGuItem extends TendedGuItem {

    private static final String FAILED_STRENGTH_HELD = "guzhenren.item.failed.beast_strength_held";

    private final BeastStrength beast;

    public BeastStrengthGuItem(Properties properties, BeastStrength beast, GuSpec spec) {
        super(properties, spec);
        this.beast = beast;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return StrengthService.has(player, beast)
                ? new Refusal(FAILED_STRENGTH_HELD, Component.translatable(beast.getTranslationKey()))
                : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {StrengthService.grant(player, beast);}
}

package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BoarGuItem extends RefinableGuItem {

    private static final String FAILED_STRENGTH_HELD = "guzhenren.item.failed.beast_strength_held";

    private static final int PORK_UNITS = 1;

    private static final long SPECK_PER_USE = 1L;

    private static final int REFINE_COST = 1200;

    private final BeastStrength beast;

    public BoarGuItem(Properties properties, BeastStrength beast) {
        super(properties, Rank.ONE, GuPath.STRENGTH);
        this.beast = beast;
    }

    @Override
    public int refineCost() {return REFINE_COST;}

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.BOAR_FEED) ? PORK_UNITS : 0;}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return StrengthService.has(player, beast)
                ? new Refusal(FAILED_STRENGTH_HELD, Component.translatable(beast.getTranslationKey()))
                : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {StrengthService.grant(player, beast);}

    @Override
    protected long speckPerUse() {return SPECK_PER_USE;}

    @Override
    protected MarkTag speckTag() {return beast.getMarkTag();}
}
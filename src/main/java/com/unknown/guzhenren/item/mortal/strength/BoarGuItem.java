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

//  Boar Gu [豕蛊]: 36 uses buy this beast's strength, once ever, for the Beast Phantom branch.
//  ⚠ One class, two items -- registration gives the beast. Everything else is RefinableGuItem's.
public class BoarGuItem extends RefinableGuItem {

    private static final String FAILED_STRENGTH_HELD = "guzhenren.item.failed.beast_strength_held";

    //  Pork is worth one unit, and four units buy a hunger point -- the classic four-pork rate.
    private static final int PORK_UNITS = 1;

    //  Both boars are Rank I, so there is no ladder here: one speck a use, flat.
    private static final long SPECK_PER_USE = 1L;

    //  1.5× a Rank I peak Ten-Extremes pool (800), the same ratio the four Human Jun Gu carry.
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

    //  ⚠ The SPECIES' tag, read off the beast itself -- a tiger Gu would need no line here at all.
    @Override
    protected MarkTag speckTag() {return beast.getMarkTag();}
}
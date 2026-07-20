package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  Jin Strength Gu (斤力蛊): 36 uses buy one 斤之力, up to nine, for the Human Jun branch.
//  ⚠ Two feed rates, so two tags -- a tag carries one rate for every member.
public class JinStrengthGuItem extends RefinableGuItem {

    private static final String FAILED_JUN_FULL = "guzhenren.item.failed.jun_strength_full";

    //  Four units buy a hunger point, so raw iron is one hunger each and a block is nine.
    private static final int IRON_UNITS = 4;
    private static final int IRON_BLOCK_UNITS = 36;
    private static final int JUN_PER_GRANT = 1;

    public JinStrengthGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.STRENGTH);
    }

    @Override
    protected int feedUnits(ItemStack food) {
        if (food.is(ModItemTags.JIN_FEED_DENSE)) return IRON_BLOCK_UNITS;
        return food.is(ModItemTags.JIN_FEED) ? IRON_UNITS : 0;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player) {
        return StrengthService.jun(player, JunStrength.JIN) >= JunStrength.MAX_PER_KIND
                ? new Refusal(FAILED_JUN_FULL)
                : null;
    }

    @Override
    protected void payout(ServerPlayer player) {
        StrengthService.addJun(player, JunStrength.JIN, JUN_PER_GRANT);
    }
}

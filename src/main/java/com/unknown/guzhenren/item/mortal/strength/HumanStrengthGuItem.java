package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  The Human Jun Strength Branch [人力钧力流], one class for all four ranks: registration gives the rank
//  and the kind, and every number falls out of the rank -- the LiquorWorm/Relics shape.
//  ⚠ Ranks III-IV eat SMELTED iron (ingot/block); I-II eat raw iron. Refine cost climbs ×10 a rank.
public class HumanStrengthGuItem extends RefinableGuItem {

    private static final String FAILED_LAYERS_FULL = "guzhenren.item.failed.human_strength_full";

    //  An iron item is four units, a block nine of those (36). unitsPerHunger climbs so the same iron
    //  feeds a higher-rank Gu more slowly -- every value divides 36, so a feed lands on a whole point.
    private static final int IRON_UNITS = 4;
    private static final int IRON_BLOCK_UNITS = 36;
    private static final int[] UNITS_PER_HUNGER = {4, 12, 12, 36};

    private static final int LAYERS_PER_GRANT = 1;
    private static final int USES_PER_LAYER = 18;
    //  1.5× the peak Ten-Extremes pool of its own rank (800 / 8k / 80k / 800k), flat across the ladder.
    private static final int BASE_REFINE_COST = 1200;
    private static final int BASE_SPECK_PER_USE = 1;
    private static final int SPECK_LADDER = 4;

    private final HumanStrength kind;

    public HumanStrengthGuItem(Properties properties, Rank rank, HumanStrength kind) {
        super(properties, rank, GuPath.STRENGTH);
        this.kind = kind;
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, 10, tier());}

    @Override
    public int usesPerGrant() {return USES_PER_LAYER;}

    @Override
    protected int unitsPerHunger() {return UNITS_PER_HUNGER[tier()];}

    //  1 / 4 / 16 / 64 a use, climbing ×4 a rank. ⚠ Booked under the branch's own tag, see speckTag().
    @Override
    protected long speckPerUse() {return scaled(BASE_SPECK_PER_USE, SPECK_LADDER, tier());}
    //endregion

    //  Raw iron for Ranks I-II, smelted iron for III-IV; a block is worth nine ingots either way.
    @Override
    protected int feedUnits(ItemStack food) {
        boolean smelted = tier() >= 2;
        TagKey<Item> normal = smelted ? ModItemTags.JIN_FEED_SMELTED : ModItemTags.JIN_FEED;
        TagKey<Item> dense = smelted ? ModItemTags.JIN_FEED_SMELTED_DENSE : ModItemTags.JIN_FEED_DENSE;
        if (food.is(dense)) return IRON_BLOCK_UNITS;
        return food.is(normal) ? IRON_UNITS : 0;
    }

    //  ⚠ The ceiling is the KIND's, not one number for all four -- 钧 and 十钧 hold 30 layers, 斤 and
    //  十斤 nine. The message carries it, or the refusal would name a limit that is not this Gu's.
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

    //  These specks are the branch's own, so a later system can revoke or convert exactly them.
    @Override
    protected MarkTag speckTag() {return MarkTag.STRENGTH_HUMAN;}
}
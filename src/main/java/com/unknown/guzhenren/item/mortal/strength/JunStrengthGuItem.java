package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//  The Human Jun Strength Branch [人力钧力流], one class for all four ranks: registration gives the rank
//  and the Jun kind, and every number falls out of the rank -- the LiquorWorm/Relics shape.
//  ⚠ Ranks III-IV eat SMELTED iron (ingot/block); I-II eat raw iron. Refine cost climbs ×10 a rank.
public class JunStrengthGuItem extends RefinableGuItem {

    private static final String FAILED_JUN_FULL = "guzhenren.item.failed.jun_strength_full";

    //  An iron item is four units, a block nine of those (36). unitsPerHunger climbs so the same iron
    //  feeds a higher-rank Gu more slowly -- every value divides 36, so a feed lands on a whole point.
    private static final int IRON_UNITS = 4;
    private static final int IRON_BLOCK_UNITS = 36;
    private static final int[] UNITS_PER_HUNGER = {4, 12, 12, 36};

    private static final int JUN_PER_GRANT = 1;
    private static final int USES_PER_LAYER = 18;
    private static final int BASE_REFINE_COST = 640;
    private static final int BASE_REFINE_PER_USE = 100;

    private final JunStrength kind;

    public JunStrengthGuItem(Properties properties, Rank rank, JunStrength kind) {
        super(properties, rank, GuPath.STRENGTH);
        this.kind = kind;
    }

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, 10, tier());}

    @Override
    protected int refinePerUse() {return scaled(BASE_REFINE_PER_USE, 10, tier());}

    @Override
    public int usesPerGrant() {return USES_PER_LAYER;}

    @Override
    protected int unitsPerHunger() {return UNITS_PER_HUNGER[tier()];}
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

    @Override
    protected @Nullable Refusal payoutGate(Player player) {
        return StrengthService.jun(player, kind) >= JunStrength.MAX_PER_KIND
                ? new Refusal(FAILED_JUN_FULL)
                : null;
    }

    @Override
    protected void payout(ServerPlayer player) {StrengthService.addJun(player, kind, JUN_PER_GRANT);}
}

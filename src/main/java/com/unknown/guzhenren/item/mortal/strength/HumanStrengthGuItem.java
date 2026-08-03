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

public class HumanStrengthGuItem extends RefinableGuItem {

    private static final String FAILED_LAYERS_FULL = "guzhenren.item.failed.human_strength_full";

    private static final int IRON_UNITS = 4;
    private static final int IRON_BLOCK_UNITS = 36;
    private static final int[] UNITS_PER_HUNGER = {4, 12, 12, 36};

    private static final int LAYERS_PER_GRANT = 1;
    private static final int USES_PER_LAYER = 18;
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

    @Override
    protected long speckPerUse() {return scaled(BASE_SPECK_PER_USE, SPECK_LADDER, tier());}
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {
        boolean smelted = tier() >= 2;
        TagKey<Item> normal = smelted ? ModItemTags.JIN_FEED_SMELTED : ModItemTags.JIN_FEED;
        TagKey<Item> dense = smelted ? ModItemTags.JIN_FEED_SMELTED_DENSE : ModItemTags.JIN_FEED_DENSE;
        if (food.is(dense)) return IRON_BLOCK_UNITS;
        return food.is(normal) ? IRON_UNITS : 0;
    }

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        int base = super.useDurationTicks(player, stack);
        return base > 0 && refined(stack) ? PHYSIQUE_USE_TICKS : base;
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

    @Override
    protected MarkTag speckTag() {return MarkTag.STRENGTH_HUMAN;}
}
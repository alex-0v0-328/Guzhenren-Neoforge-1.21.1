package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.RefinableGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AllOutEffortGuItem extends RefinableGuItem {

    private static final String FAILED_ALREADY_UNLEASHED = "guzhenren.item.failed.all_out_active";
    private static final String TOOLTIP_HUNGER = "guzhenren.item.gu.hunger_progress";

    private static final int STONE_UNITS = 5;
    private static final int MAX_HUNGER = 20;
    private static final int BASE_REFINE_COST = 160_000;
    private static final int REFINE_LADDER = 10;

    private static final int[] UNITS_PER_HUNGER = {  8,  12,  16};
    private static final int[] HUNGER_PER_USE   = {  5,   4,   2};
    private static final int[] EFFECT_SECONDS   = { 60,  90, 120};
    private static final int[] COOLDOWN_SECONDS = { 80, 100, 120};

    public AllOutEffortGuItem(Properties properties, Rank rank) {
        super(properties, rank, GuPath.STRENGTH);
    }

    private int rung() {return rank().ordinal() - Rank.THREE.ordinal();}

    //region the numbers this Gu bends
    @Override
    public int refineCost() {return scaled(BASE_REFINE_COST, REFINE_LADDER, rung());}

    @Override
    protected int maxHunger() {return MAX_HUNGER;}

    @Override
    protected int hungerPerUse(ItemStack stack) {return HUNGER_PER_USE[rung()];}

    @Override
    protected int unitsPerHunger() {return UNITS_PER_HUNGER[rung()];}

    @Override
    public int usesPerGrant() {return 1;}

    @Override
    protected int useCooldownTicks() {return COOLDOWN_SECONDS[rung()] * Ticks.SECOND;}
    //endregion

    @Override
    protected int feedUnits(ItemStack food) {return food.is(ModItemTags.ALL_OUT_FEED) ? STONE_UNITS : 0;}

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return player.hasEffect(ModEffects.ALL_OUT_EFFORT) ? new Refusal(FAILED_ALREADY_UNLEASHED) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.ALL_OUT_EFFORT, EFFECT_SECONDS[rung()] * Ticks.SECOND));
        AttackService.refresh(player);
    }

    @Override
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_HUNGER, state(stack).hunger(), maxHunger())
                : super.progressLine(stack);
    }
}

package com.unknown.guzhenren.item.mortal.strength;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModItemTags;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  Jin Strength Gu (斤力蛊): wild until refined, then fed raw iron and used 36 times for one 斤之力.
//  ⚠ The Human branch counts, it does not collect -- nine 斤 per person, so nine Gu and 324 uses to cap.
public class JinStrengthGuItem extends MortalGuItem {

    private static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    private static final String FAILED_REFINE_ESSENCE = "guzhenren.item.failed.refine_essence";
    private static final String FAILED_JUN_FULL = "guzhenren.item.failed.jun_strength_full";
    private static final String FAILED_HUNGRY = "guzhenren.item.failed.gu_hungry";
    private static final String TOOLTIP_USES = "guzhenren.item.gu.uses";
    private static final String MSG_HUNGRY = "guzhenren.item.gu.hungry";
    private static final String MSG_STARVED = "guzhenren.item.gu.starved";

    private static final int REFINE_PER_USE = 100;
    private static final int REFINE_COOLDOWN_TICKS = 20;
    private static final int HUNGRY_THRESHOLD = 2;
    private static final long SPECK_PER_USE = 1L;
    private static final int JUN_PER_GU = 1;

    //  ⚠ Two rates, so two tags: one tag carries one rate for every member, and a block is worth nine.
    private static final int PLAIN_FEED_VALUE = 1;
    private static final int DENSE_FEED_VALUE = 9;

    //  Rank I, Strength Path, reusable and feedable -- the Boar Gu's shape, a different branch's payout.
    public JinStrengthGuItem(Properties properties) {
        super(properties, Rank.ONE, GuPath.STRENGTH, true, true);
    }

    @Override
    protected boolean hasUse() {return true;}
    private static DataComponentType<JinState> type() {return ModDataComponents.JIN_STATE.get();}
    private static JinState state(ItemStack s) {return s.getOrDefault(type(), JinState.WILD);}
    private static void store(ItemStack s, JinState v) {s.set(type(), v);}

    //  Slow while it is being bound, quick once it answers to you.
    @Override
    protected int cooldownTicks(ItemStack stack) {
        return state(stack).refined() ? COOLDOWN_TICKS : REFINE_COOLDOWN_TICKS;
    }

    //  Wild: refining needs essence. Refined: nine 斤 is the ceiling, and a starving Gu will not work.
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        JinState state = state(stack);
        if (!state.refined()) {
            if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
            return EssenceService.currentEssence(player) <= 0L ? new Refusal(FAILED_REFINE_ESSENCE) : null;
        }
        if (StrengthService.jun(player, JunStrength.JIN) >= JunStrength.MAX_PER_KIND) {
            return new Refusal(FAILED_JUN_FULL);
        }
        //  Hunger 1 still works if the iron in the other hand lifts it first -- apply() feeds before it uses.
        return state.hunger() + feedPoints(player, state) <= 1 ? new Refusal(FAILED_HUNGRY) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        JinState state = state(stack);
        //  Refining never spends the Gu -- that is what the 0 says.
        if (!state.refined()) {
            refineStep(player, stack, state);
            return 0;
        }

        //  Food in the other hand feeds and uses in one click -- feeding first, so hunger 1 is survivable.
        int points = feedPoints(player, state);
        if (points > 0) {
            eat(player, points);
            state = state.fed(points);
        }
        state = state.used();
        PathService.addSpeck(player, GuPath.STRENGTH, SPECK_PER_USE);

        //  The 36th pays out and the count starts over -- useCount is progress toward the next 斤.
        if (state.grantDue()) {
            StrengthService.addJun(player, JunStrength.JIN, JUN_PER_GU);
            state = state.afterGrant();
        }
        store(stack, state);

        //  ⚠ Never consumed. Nothing about the Gu is spent -- it stays refined, stays fed, and once its
        //  owner is capped it can be handed to another player. Starving is its only end.
        return 0;
    }

    //  Feeds only when it actually can: bound, its food in the other hand, and room to eat.
    //  ⚠ Silent when it cannot -- the bar rising is the feedback, and a swing is not a command.
    @Override
    protected boolean hasSwing(Player player, ItemStack stack) {
        JinState state = state(stack);
        return feedable() && state.refined() && feedPoints(player, state) > 0;
    }

    @Override
    protected int swingApply(ServerPlayer player, ItemStack stack) {
        JinState state = state(stack);
        int points = feedPoints(player, state);
        eat(player, points);
        store(stack, state.fed(points));
        return 0;
    }

    //  野生·斤力蛊 until it answers to someone.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return state(stack).refined() ? super.getName(stack) : ModDisplayText.wild(super.getName(stack));
    }

    //  The one number the bar cannot show: how many of the 36 uses are gone.
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        JinState state = state(stack);
        if (!state.refined()) return;
        tooltip.add(Component.translatable(TOOLTIP_USES, state.useCount(), JinState.USES_PER_GRANT)
                .withStyle(ChatFormatting.GRAY));
    }

    //  Wild: how far refined. Refined: how fed. One bar, two meanings -- the name says which you are reading.
    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        JinState state = state(stack);
        return state.refined() ? state.hunger() < JinState.MAX_HUNGER : state.refineProgress() > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {return Math.round(fraction(state(stack)) * 13.0F);}

    @Override
    public int getBarColor(@NotNull ItemStack stack) {return Mth.hsvToRgb(fraction(state(stack)) / 3.0F, 1.0F, 1.0F);}

    //  One hunger per elapsed day, every refined jin Gu he carries. Called off the day rollover, so
    //  ⚠ days can be well over 1, and a wild Gu must be skipped -- its hunger of 0 would read as starved.
    public static void starve(ServerPlayer player, long days) {
        int decay = (int) Math.min(days, JinState.MAX_HUNGER);
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof JinStrengthGuItem)) continue;
            JinState state = state(stack);
            if (!state.refined()) continue;

            JinState next = state.decayed(decay);
            if (next.starved()) {
                inventory.setItem(slot, ItemStack.EMPTY);
                announce(player, stack, MSG_STARVED);
                continue;
            }
            store(stack, next);
            if (next.hunger() <= HUNGRY_THRESHOLD) announce(player, stack, MSG_HUNGRY);
        }
    }

    //  Invest what he can spare, capped per attempt -- a trickle still gets there, it just takes longer.
    private void refineStep(ServerPlayer player, ItemStack stack, JinState state) {
        int remaining = JinState.REFINE_COST - state.refineProgress();
        int invest = (int) Math.min(Math.min(REFINE_PER_USE, remaining), EssenceService.currentEssence(player));
        if (invest <= 0) return;

        EssenceService.consume(player, invest);
        store(stack, state.refine(invest));
    }

    //  What one item in the other hand is worth: a block is nine of the plain one, and nothing else feeds.
    private static int feedValue(ItemStack food) {
        if (food.is(ModItemTags.JIN_FEED_DENSE)) return DENSE_FEED_VALUE;
        return food.is(ModItemTags.JIN_FEED) ? PLAIN_FEED_VALUE : 0;
    }

    //  Whole items only: a block that does not fit entirely stays in his hand rather than being wasted.
    private static int feedPoints(Player player, JinState state) {
        ItemStack food = player.getOffhandItem();
        int value = feedValue(food);
        if (value <= 0) return 0;
        return Math.min(food.getCount(), (JinState.MAX_HUNGER - state.hunger()) / value) * value;
    }

    //  ⚠ Creative pays no iron, exactly as it pays no Gu -- see GuItem.spend.
    private static void eat(ServerPlayer player, int points) {
        ItemStack food = player.getOffhandItem();
        int value = feedValue(food);
        if (points > 0 && value > 0 && !player.hasInfiniteMaterials()) food.shrink(points / value);
    }

    private static float fraction(JinState state) {
        return state.refined()
                ? state.hunger() / (float) JinState.MAX_HUNGER
                : state.refineProgress() / (float) JinState.REFINE_COST;
    }

    //  Plain chat, uncolored: nothing was refused, and the action bar gets missed while he is busy.
    private static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }
}

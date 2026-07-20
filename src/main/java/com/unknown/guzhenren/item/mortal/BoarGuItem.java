package com.unknown.guzhenren.item.mortal;

import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
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

//  Boar Gu (豕蛊): wild until refined, then fed raw pork and used 36 times for a beast strength.
//  ⚠ One class, two items -- registration gives the beast. See CLAUDE.md "Items".
public class BoarGuItem extends MortalGuItem {

    private static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    private static final String FAILED_REFINE_ESSENCE = "guzhenren.item.failed.boar_refine_essence";
    private static final String FAILED_STRENGTH_HELD = "guzhenren.item.failed.beast_strength_held";
    private static final String FAILED_HUNGRY = "guzhenren.item.failed.boar_hungry";
    private static final String TOOLTIP_USES = "guzhenren.item.boar.uses";
    private static final String MSG_HUNGRY = "guzhenren.item.boar.hungry";
    private static final String MSG_STARVED = "guzhenren.item.boar.starved";

    private static final int REFINE_PER_USE = 50;
    private static final int REFINE_COOLDOWN_TICKS = 20;
    private static final int PORK_PER_HUNGER = 4;
    private static final int HUNGRY_THRESHOLD = 2;
    private static final long SPECK_PER_USE = 1L;

    private final BeastStrength beast;

    //  Rank I, Strength Path, reusable and feedable -- the first Gu that is either.
    public BoarGuItem(Properties properties, BeastStrength beast) {
        super(properties, Rank.ONE, GuPath.STRENGTH, true, true);
        this.beast = beast;
    }

    @Override
    protected boolean hasUse() {return true;}
    private static DataComponentType<BoarState> type() {return ModDataComponents.BOAR_STATE.get();}
    private static BoarState state(ItemStack s) {return s.getOrDefault(type(), BoarState.WILD);}
    private static void store(ItemStack s, BoarState v) {s.set(type(), v);}

    //  Slow while it is being bound, quick once it answers to you.
    @Override
    protected int cooldownTicks(ItemStack stack) {
        return state(stack).refined() ? COOLDOWN_TICKS : REFINE_COOLDOWN_TICKS;
    }

    //  Wild: refining needs essence. Refined: one beast strength per player, and a starving Gu will not work.
    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        BoarState state = state(stack);
        if (!state.refined()) {
            if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
            return EssenceService.currentEssence(player) <= 0L ? new Refusal(FAILED_REFINE_ESSENCE) : null;
        }
        if (StrengthService.has(player, beast)) {
            return new Refusal(FAILED_STRENGTH_HELD, Component.translatable(beast.getTranslationKey()));
        }
        //  Hunger 1 still works if the pork in the other hand lifts it first -- apply() feeds before it uses.
        return state.hunger() + feedPoints(player, state) <= 1 ? new Refusal(FAILED_HUNGRY) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        BoarState state = state(stack);
        if (!state.refined()) return refineStep(player, stack, state);

        //  Food in the other hand feeds and uses in one click -- feeding first, so hunger 1 is survivable.
        int points = feedPoints(player, state);
        if (points > 0) {
            eat(player, points);
            state = state.fed(points);
        }
        state = state.used();
        PathService.addSpeck(player, GuPath.STRENGTH, SPECK_PER_USE);
        store(stack, state);
        if (!state.spent()) return 0;

        //  reusable: a use spends nothing. The 36th is not a use -- it is the Gu being used up.
        StrengthService.grant(player, beast);
        return 1;
    }

    //  Feeds only when it actually can: bound, its food in the other hand, and room to eat.
    //  ⚠ Silent when it cannot -- the bar rising is the feedback, and a swing is not a command.
    @Override
    protected boolean hasSwing(Player player, ItemStack stack) {
        BoarState state = state(stack);
        return feedable() && state.refined() && feedPoints(player, state) > 0;
    }

    @Override
    protected int swingApply(ServerPlayer player, ItemStack stack) {
        BoarState state = state(stack);
        int points = feedPoints(player, state);
        eat(player, points);
        store(stack, state.fed(points));
        return 0;
    }

    //  野生·黑豕蛊 until it answers to someone.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return state(stack).refined() ? super.getName(stack) : ModDisplayText.wild(super.getName(stack));
    }

    //  The one number the bar cannot show: how many of the 36 uses are gone.
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        BoarState state = state(stack);
        if (!state.refined()) return;
        tooltip.add(Component.translatable(TOOLTIP_USES, state.useCount(), BoarState.USES_TO_CONSUME)
                .withStyle(ChatFormatting.GRAY));
    }

    //  Wild: how far refined. Refined: how fed. One bar, two meanings -- the name says which you are reading.
    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        BoarState state = state(stack);
        return state.refined() ? state.hunger() < BoarState.MAX_HUNGER : state.refineProgress() > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {return Math.round(fraction(state(stack)) * 13.0F);}

    @Override
    public int getBarColor(@NotNull ItemStack stack) {return Mth.hsvToRgb(fraction(state(stack)) / 3.0F, 1.0F, 1.0F);}

    //  One hunger per elapsed day, every refined boar Gu he carries. Called off the day rollover, so
    //  ⚠ days can be well over 1, and a wild Gu must be skipped -- its hunger of 0 would read as starved.
    public static void starve(ServerPlayer player, long days) {
        int decay = (int) Math.min(days, BoarState.MAX_HUNGER);
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof BoarGuItem)) continue;
            BoarState state = state(stack);
            if (!state.refined()) continue;

            BoarState next = state.decayed(decay);
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
    private int refineStep(ServerPlayer player, ItemStack stack, BoarState state) {
        int remaining = BoarState.REFINE_COST - state.refineProgress();
        int invest = (int) Math.min(Math.min(REFINE_PER_USE, remaining), EssenceService.currentEssence(player));
        if (invest <= 0) return 0;

        EssenceService.consume(player, invest);
        store(stack, state.refine(invest));
        return 0;
    }

    //  Whole points only: four pork buy one, and the remainder stays in his hand.
    private static int feedPoints(Player player, BoarState state) {
        ItemStack food = player.getOffhandItem();
        if (!food.is(ModItemTags.BOAR_FEED)) return 0;
        return Math.min(BoarState.MAX_HUNGER - state.hunger(), food.getCount() / PORK_PER_HUNGER);
    }

    //  ⚠ Creative pays no pork, exactly as it pays no Gu -- see GuItem.spend.
    private static void eat(ServerPlayer player, int points) {
        if (points > 0 && !player.hasInfiniteMaterials()) player.getOffhandItem().shrink(points * PORK_PER_HUNGER);
    }

    private static float fraction(BoarState state) {
        return state.refined()
                ? state.hunger() / (float) BoarState.MAX_HUNGER
                : state.refineProgress() / (float) BoarState.REFINE_COST;
    }

    //  Plain chat, uncolored: nothing was refused, and the action bar gets missed while he is busy.
    private static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }
}

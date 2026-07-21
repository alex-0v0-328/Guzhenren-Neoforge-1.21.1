package com.unknown.guzhenren.item;

import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.registry.ModDataComponents;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//  A Gu that is 野生 until refined, then fed and used: 豕蛊, 斤力蛊, and everything shaped like them.
//  ⚠ It is NEVER consumed -- starving is its only end, so a capped owner can hand it to another player.
public abstract class RefinableGuItem extends MortalGuItem {

    protected static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    protected static final String FAILED_REFINE_ESSENCE = "guzhenren.item.failed.refine_essence";
    protected static final String FAILED_HUNGRY = "guzhenren.item.failed.gu_hungry";
    private static final String TOOLTIP_USES = "guzhenren.item.gu.uses";
    private static final String MSG_HUNGRY = "guzhenren.item.gu.hungry";
    private static final String MSG_STARVED = "guzhenren.item.gu.starved";

    protected RefinableGuItem(Properties properties, Rank rank, GuPath path) {
        super(properties, rank, path, true, true);
    }

    //region the numbers a leaf may bend
    protected int refineCost() {return 1000;}
    protected int refinePerUse() {return 100;}
    protected int maxHunger() {return 18;}
    protected int usesPerGrant() {return 36;}
    protected int hungryThreshold() {return 2;}
    protected int refineCooldownTicks() {return 20;}
    protected long speckPerUse() {return 1L;}

    //  ⚠ Units, not hunger: 4 units buy one hunger point. That is what lets one formula serve both
    //  "four pork = one hunger" (1 unit each) and "one raw iron = one hunger" (4 units each).
    protected int unitsPerHunger() {return 4;}
    //endregion

    //region what a leaf must answer
    //  Units one item of this food is worth here; 0 means it is not food for this Gu.
    protected abstract int feedUnits(ItemStack food);

    //  Why this player cannot use it right now -- typically "you already hold what it grants".
    protected abstract @Nullable Refusal payoutGate(Player player);

    //  What the 36th use buys. Runs once per completed cycle.
    protected abstract void payout(ServerPlayer player);
    //endregion

    //region state
    public static RefinedGuState state(ItemStack s) {
        return s.getOrDefault(ModDataComponents.REFINED_GU_STATE.get(), RefinedGuState.WILD);
    }

    protected void store(ItemStack stack, RefinedGuState state) {
        stack.set(ModDataComponents.REFINED_GU_STATE.get(), clamp(state));
    }

    //  ⚠ The record only keeps values non-negative; the ceilings are the item's, so they are applied here.
    private RefinedGuState clamp(RefinedGuState s) {
        return new RefinedGuState(
                Math.min(s.refineProgress(), refineCost()),
                Math.min(s.useCount(), usesPerGrant()),
                Math.min(s.hunger(), maxHunger()));
    }

    public boolean refined(ItemStack stack) {return state(stack).refineProgress() >= refineCost();}
    //  What "蛊饿了" means. The threshold stays the item's business; this is the question callers ask.
    //  ⚠ Starvation is not asked, it is reported: decay() returns it, because only the biller knows.
    public boolean hungry(ItemStack stack) {return refined(stack) && state(stack).hunger() <= hungryThreshold();}
    //endregion

    //region the two clicks
    @Override
    protected boolean hasUse() {return true;}

    //  Slow while it is being bound, quick once it answers to you.
    @Override
    protected int cooldownTicks(ItemStack stack) {
        return refined(stack) ? COOLDOWN_TICKS : refineCooldownTicks();
    }

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        if (!refined(stack)) {
            if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
            return EssenceService.currentEssence(player) <= 0L ? new Refusal(FAILED_REFINE_ESSENCE) : null;
        }
        Refusal held = payoutGate(player);
        if (held != null) return held;

        //  Hunger 1 still works if the food in the other hand lifts it first -- apply() feeds before it uses.
        return state(stack).hunger() + feed(player, stack).hunger() <= 1 ? new Refusal(FAILED_HUNGRY) : null;
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) {
            refineStep(player, stack);
            return 0;
        }

        eat(player, stack);
        RefinedGuState state = state(stack);
        state = state.withUses(state.useCount() + 1).withHunger(state.hunger() - 1);
        PathService.addSpeck(player, path(), speckPerUse());

        //  The 36th pays out and the count starts over -- useCount is progress toward the NEXT payout.
        if (state.useCount() >= usesPerGrant()) {
            payout(player);
            state = state.withUses(0);
        }
        store(stack, state);
        return 0;
    }

    //  Feeds only when it can: bound, its food in the other hand, room to eat.
    //  ⚠ Silent when it cannot -- the bar rising is the feedback, and a swing is not a command.
    @Override
    protected boolean hasSwing(Player player, ItemStack stack) {
        return refined(stack) && feed(player, stack).hunger() > 0;
    }

    @Override
    protected int swingApply(ServerPlayer player, ItemStack stack) {
        eat(player, stack);
        return 0;
    }
    //endregion

    //region feeding
    //  How much the offhand can give this Gu right now, and what that costs in items.
    private Feed feed(Player player, ItemStack stack) {
        ItemStack food = player.getOffhandItem();
        int units = feedUnits(food);
        if (units <= 0) return Feed.NONE;

        int per = unitsPerHunger();
        int room = maxHunger() - state(stack).hunger();

        //  Whole items only, and never more than the room absorbs -- a block that does not fit stays put.
        int items = Math.min(food.getCount(), room * per / units);
        int hunger = items * units / per;
        if (hunger <= 0) return Feed.NONE;

        //  Only the items those whole hunger points actually cost; the remainder stays in his hand.
        return new Feed(hunger, (hunger * per + units - 1) / units);
    }

    //  ⚠ Creative pays no food, exactly as it pays no Gu -- see GuItem.spend.
    private void eat(ServerPlayer player, ItemStack stack) {
        Feed fed = feed(player, stack);
        if (fed.hunger() <= 0) return;

        if (!player.hasInfiniteMaterials()) player.getOffhandItem().shrink(fed.items());
        RefinedGuState state = state(stack);
        store(stack, state.withHunger(state.hunger() + fed.hunger()));
    }

    private record Feed(int hunger, int items) {
        private static final Feed NONE = new Feed(0, 0);
    }
    //endregion

    //region refining
    //  Invest what he can spare, capped per attempt -- a trickle still gets there, it just takes longer.
    private void refineStep(ServerPlayer player, ItemStack stack) {
        RefinedGuState state = state(stack);
        int remaining = refineCost() - state.refineProgress();
        int invest = (int) Math.min(Math.min(refinePerUse(), remaining), EssenceService.currentEssence(player));
        if (invest <= 0) return;

        EssenceService.consume(player, invest);
        int next = state.refineProgress() + invest;

        //  Completing it hands back a half-fed Gu, so the owner starts on the feeding clock.
        store(stack, next >= refineCost()
                ? new RefinedGuState(refineCost(), 0, maxHunger() / 2)
                : state.withRefine(next));
    }
    //endregion

    //region display
    //  野生·黑豕蛊 until it answers to someone.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return refined(stack) ? super.getName(stack) : ModDisplayText.wild(super.getName(stack));
    }

    //  The one number the bar cannot show: how far along this cycle is.
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!refined(stack)) return;
        tooltip.add(Component.translatable(TOOLTIP_USES, state(stack).useCount(), usesPerGrant())
                .withStyle(ChatFormatting.GRAY));
    }

    //  Wild: how far refined. Refined: how fed. One bar, two meanings -- the name says which you read.
    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return refined(stack) ? state(stack).hunger() < maxHunger() : state(stack).refineProgress() > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {return Math.round(fraction(stack) * 13.0F);}

    @Override
    public int getBarColor(@NotNull ItemStack stack) {return Mth.hsvToRgb(fraction(stack) / 3.0F, 1.0F, 1.0F);}

    private float fraction(ItemStack stack) {
        RefinedGuState s = state(stack);
        return refined(stack)
                ? s.hunger() / (float) maxHunger()
                : s.refineProgress() / (float) refineCost();
    }
    //endregion

    //region the day clock
    //  ⚠ ONE walk for every refinable Gu, so a new one needs no line anywhere. The old per-class statics
    //  each needed their own call in PlayerTickEvents, and forgetting it meant that Gu never starved.
    public static void starveAll(ServerPlayer player, long days) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof RefinableGuItem gu) || !gu.refined(stack)) continue;

            if (gu.decay(stack, days)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                starved(player, stack);
            } else if (gu.hungry(stack)) {
                announce(player, stack, MSG_HUNGRY);
            }
        }
    }

    //  Bills the elapsed days against one Gu. True means it starved. ⚠ days can be far over 1.
    public boolean decay(ItemStack stack, long days) {
        RefinedGuState state = state(stack);
        int left = state.hunger() - (int) Math.min(days, maxHunger());
        if (left <= 0) return true;

        store(stack, state.withHunger(left));
        return false;
    }

    //  Feeds one stored Gu out of the player's own inventory, once it is hungry.
    //  ⚠ Decay has already been billed by the caller, so "food means no hunger lost" is the NET effect,
    //  not a skipped tick -- the rule stays identical to hand-feeding, only the hand is automatic.
    //  Returns true if anything was eaten. ⚠ Scans the inventory only; Curios slots are not covered.
    public boolean autoFeed(ServerPlayer player, ItemStack stack) {
        if (!hungry(stack)) return false;

        Inventory inventory = player.getInventory();
        boolean ate = false;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack food = inventory.getItem(slot);
            int units = feedUnits(food);
            if (units <= 0) continue;

            int per = unitsPerHunger();
            int room = maxHunger() - state(stack).hunger();
            if (room <= 0) break;

            int items = Math.min(food.getCount(), room * per / units);
            int hunger = items * units / per;
            if (hunger <= 0) continue;

            food.shrink((hunger * per + units - 1) / units);
            store(stack, state(stack).withHunger(state(stack).hunger() + hunger));
            ate = true;
        }
        return ate;
    }

    //  Plain chat, uncolored: nothing was refused, and the action bar gets missed while he is busy.
    public static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }

    public static void announceHungry(ServerPlayer player, ItemStack stack) {announce(player, stack, MSG_HUNGRY);}

    //  It starved. Whoever was carrying it hears so; if it was someone's Vital Gu, its OWNER pays -- which
    //  is why the mark stores a UUID and not a flag. ⚠ An offline owner is not billed and nothing is
    //  queued: this mod has no pending-penalty concept, and one case does not justify inventing it.
    public static void starved(ServerPlayer holder, ItemStack stack) {
        announce(holder, stack, MSG_STARVED);
        UUID uuid = owner(stack);
        if (uuid == null) return;

        ServerPlayer owner = holder.server.getPlayerList().getPlayer(uuid);
        if (owner != null) PlayerDataService.onVitalGuLost(owner, stack);
    }
    //endregion
}

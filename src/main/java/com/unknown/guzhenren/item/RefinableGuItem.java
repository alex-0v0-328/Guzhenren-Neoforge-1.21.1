package com.unknown.guzhenren.item;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.registry.ModDataComponents;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RefinableGuItem extends MortalGuItem {

    protected static final String FAILED_UNAWAKENED = "guzhenren.item.failed.unawakened";
    protected static final String FAILED_REFINE_ESSENCE = "guzhenren.item.failed.refine_essence";
    private static final String TOOLTIP_USES = "guzhenren.item.gu.uses";
    private static final String TOOLTIP_REFINE = "guzhenren.item.gu.refine_progress";
    private static final String MSG_HUNGRY = "guzhenren.item.gu.hungry";
    private static final String MSG_STARVED = "guzhenren.item.gu.starved";
    private static final String MSG_EXHAUSTED = "guzhenren.item.gu.exhausted";

    protected RefinableGuItem(Properties properties, Rank rank, GuPath path) {
        super(properties, rank, path, true, true);
    }

    //region the numbers a leaf may bend
    public int refineCost() {return 640;}
    protected int refineMinEssence() {return 20;}
    protected int maxHunger() {return 18;}
    public int usesPerGrant() {return 36;}
    protected int hungryThreshold() {return 1;}
    protected int hungerPerUse(ItemStack stack) {return 1;}
    protected long speckPerUse() {return 0L;}

    @Override
    public Component chargeCaption(ItemStack stack) {
        RefinedGuState state = state(stack);
        return refined(stack)
                ? Component.translatable("guzhenren.hud.using", state.useCount(), usesPerGrant())
                : Component.translatable("guzhenren.hud.refining", state.refineProgress(), refineCost());
    }

    protected MarkTag speckTag() {return MarkTag.NATURAL;}
    protected int tier() {return rank().ordinal() - Rank.ONE.ordinal();}

    protected static int scaled(int base, int factor, int tier) {
        int value = base;
        for (int i = 0; i < tier; i++) value *= factor;
        return value;
    }

    protected int chargeTicks() {return 60;}
    protected int fastChargeTicks() {return 20;}
    protected int slowChargeTicks() {return 180;}
    protected int unitsPerHunger() {return 4;}
    //endregion

    //region the fed clock -- Liquor Worm [酒虫] and Primeval Elder Gu [元老蛊] only
    public static final int FED_WINDOW_TICKS = 2 * Ticks.DAY;
    public static final int WARN_AFTER_TICKS = FED_WINDOW_TICKS + Ticks.HALF_DAY;
    public static final int DEATH_AFTER_TICKS = FED_WINDOW_TICKS + Ticks.DAY;

    public static final int FED_BAR_UNIT_TICKS = 1000;
    public static final int FED_BAR_UNITS = DEATH_AFTER_TICKS / FED_BAR_UNIT_TICKS;

    protected boolean usesFedClock() {return false;}
    protected int mealItems() {return 0;}

    public static long fedAt(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FED_AT.get(), 0L);
    }

    public static long now(ServerPlayer player) {
        return player.server.overworld().getDayTime();
    }

    private static long fedAge(ServerPlayer player, ItemStack stack) {
        return Math.max(0L, now(player) - fedAt(stack));
    }

    protected void stampFed(ServerPlayer player, ItemStack stack) {
        stack.set(ModDataComponents.FED_AT.get(), now(player));
        stack.set(ModDataComponents.FED_LEFT.get(), FED_BAR_UNITS);
    }

    protected boolean needsMeal(ServerPlayer player, ItemStack stack) {
        return usesFedClock() && refined(stack) && fedAge(player, stack) >= FED_WINDOW_TICKS;
    }

    public static int fedLeft(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FED_LEFT.get(), FED_BAR_UNITS);
    }

    private void refreshFedBar(ServerPlayer player, ItemStack stack) {
        long left = (DEATH_AFTER_TICKS - fedAge(player, stack)) / FED_BAR_UNIT_TICKS;
        int units = (int) Math.clamp(left, 0L, FED_BAR_UNITS);
        if (units != fedLeft(stack)) stack.set(ModDataComponents.FED_LEFT.get(), units);
    }

    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {}

    private void warnOnceFed(ServerPlayer player, ItemStack stack) {
        long fed = fedAt(stack);
        if (stack.getOrDefault(ModDataComponents.FED_WARNED.get(), Long.MIN_VALUE) == fed) return;

        stack.set(ModDataComponents.FED_WARNED.get(), fed);
        announce(player, stack, MSG_HUNGRY);
    }
    //endregion

    //region what a leaf must answer
    protected abstract int feedUnits(ItemStack food);
    protected abstract @Nullable Refusal payoutGate(Player player, ItemStack stack);
    protected void afterUse(ServerPlayer player, ItemStack stack) {eat(player, stack);}
    protected abstract void payout(ServerPlayer player, ItemStack stack);
    //endregion

    //region state
    public static RefinedGuState state(ItemStack s) {
        return s.getOrDefault(ModDataComponents.REFINED_GU_STATE.get(), RefinedGuState.WILD);
    }

    protected void store(ItemStack stack, RefinedGuState state) {
        stack.set(ModDataComponents.REFINED_GU_STATE.get(), clamp(state));
    }

    private RefinedGuState clamp(RefinedGuState s) {
        return new RefinedGuState(
                Math.min(s.refineProgress(), refineCost()),
                Math.min(s.useCount(), usesPerGrant()),
                Math.min(s.hunger(), maxHunger()));
    }

    public boolean refined(ItemStack stack) {return state(stack).refineProgress() >= refineCost();}

    public boolean hungry(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) return false;
        return usesFedClock()
                ? fedAge(player, stack) >= WARN_AFTER_TICKS
                : state(stack).hunger() <= hungryThreshold();
    }
    //endregion

    //region the two clicks
    @Override
    protected boolean hasUse() {return true;}

    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        if (holdingFood(player, stack) && !player.isShiftKeyDown()) return 0;
        int gap = rankGap(player);
        if (gap > 0) return fastChargeTicks();
        return gap == 0 ? chargeTicks() : slowChargeTicks();
    }

    private int rankGap(Player player) {
        return ApertureService.rank(player).ordinal() - rank().ordinal();
    }

    private @Nullable Refusal common(Player player, ItemStack stack) {
        if (!ApertureService.isAwakened(player)) return new Refusal(FAILED_UNAWAKENED);
        return !refined(stack) && EssenceService.spendable(player) < refineMinEssence()
                ? new Refusal(FAILED_REFINE_ESSENCE)
                : null;
    }

    @Override
    protected @Nullable Refusal gate(Player player, ItemStack stack) {
        Refusal refusal = common(player, stack);
        if (refusal != null || !refined(stack)) return refusal;

        if (holdingFood(player, stack)) return null;
        return payoutGate(player, stack);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) {
            refineStep(player, stack);
            return 0;
        }
        if (holdingFood(player, stack)) {
            eat(player, stack);
            return 0;
        }
        return drive(player, stack);
    }

    protected int drive(ServerPlayer player, ItemStack stack) {
        boolean forced = state(stack).hunger() <= 0;

        RefinedGuState state = state(stack);
        state = state.withUses(state.useCount() + 1)
                .withHunger(state.hunger() - Math.max(1, hungerPerUse(stack)));
        if (speckPerUse() > 0) PathService.addSpeck(player, path(), speckTag(), speckPerUse());

        if (state.useCount() >= usesPerGrant()) {
            payout(player, stack);
            state = state.withUses(0);
        }
        store(stack, state);

        if (forced && !player.hasInfiniteMaterials()) {
            exhausted(player, stack);
            return 1;
        }
        afterUse(player, stack);
        if (!hungry(player, stack)) return 0;
        if (usesFedClock()) {
            warnOnceFed(player, stack);
        } else {
            announceHungry(player, stack);
        }
        return 0;
    }

    @Override
    protected boolean hasSneakUse(Player player, ItemStack stack) {return holdingFood(player, stack);}

    @Override
    protected @Nullable Refusal sneakGate(Player player, ItemStack stack) {
        Refusal refusal = common(player, stack);
        return refusal != null ? refusal : payoutGate(player, stack);
    }

    @Override
    protected int sneakApply(ServerPlayer player, ItemStack stack) {
        eat(player, stack);
        return drive(player, stack);
    }

    private boolean holdingFood(Player player, ItemStack stack) {
        return refined(stack) && feedUnits(player.getOffhandItem()) > 0;
    }
    //endregion

    //region feeding
    private Feed feed(Player player, ItemStack stack) {
        ItemStack food = player.getOffhandItem();
        int units = feedUnits(food);
        if (units <= 0) return Feed.NONE;

        int per = unitsPerHunger();
        int room = maxHunger() - state(stack).hunger();

        int items = Math.min(food.getCount(), room * per / units);
        int hunger = items * units / per;
        if (hunger <= 0) return Feed.NONE;

        return new Feed(hunger, (hunger * per + units - 1) / units);
    }

    private void eat(ServerPlayer player, ItemStack stack) {
        if (usesFedClock()) {
            eatMeal(player, stack, player.getOffhandItem());
            return;
        }
        Feed fed = feed(player, stack);
        if (fed.hunger() <= 0) return;

        if (!player.hasInfiniteMaterials()) player.getOffhandItem().shrink(fed.items());
        RefinedGuState state = state(stack);
        store(stack, state.withHunger(state.hunger() + fed.hunger()));
    }

    protected boolean eatMeal(ServerPlayer player, ItemStack stack, ItemStack food) {
        int cost = mealItems();
        if (cost <= 0 || !needsMeal(player, stack)) return false;
        if (feedUnits(food) <= 0 || food.getCount() < cost) return false;

        if (!player.hasInfiniteMaterials()) food.shrink(cost);
        stampFed(player, stack);
        return true;
    }

    private record Feed(int hunger, int items) {
        private static final Feed NONE = new Feed(0, 0);
    }
    //endregion

    //region refining
    private void refineStep(ServerPlayer player, ItemStack stack) {
        RefinedGuState state = state(stack);
        int remaining = refineCost() - state.refineProgress();
        int invest = (int) Math.min(remaining, EssenceService.spendable(player));
        if (invest <= 0) return;

        EssenceService.consume(player, invest);
        int next = state.refineProgress() + invest;

        boolean done = next >= refineCost();
        store(stack, done ? new RefinedGuState(refineCost(), 0, maxHunger()) : state.withRefine(next));

        if (done && usesFedClock()) stampFed(player, stack);
    }
    //endregion

    //region display
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return refined(stack) ? super.getName(stack) : ModDisplayText.wild(super.getName(stack));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(progressLine(stack).withStyle(ChatFormatting.GRAY));
    }

    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_USES, state(stack).useCount(), usesPerGrant())
                : Component.translatable(TOOLTIP_REFINE, state(stack).refineProgress(), refineCost());
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        if (!refined(stack)) return state(stack).refineProgress() > 0;
        return usesFedClock() ? fedLeft(stack) < FED_BAR_UNITS : state(stack).hunger() < maxHunger();
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {return Math.round(fraction(stack) * 13.0F);}

    @Override
    public int getBarColor(@NotNull ItemStack stack) {return Mth.hsvToRgb(fraction(stack) / 3.0F, 1.0F, 1.0F);}

    private float fraction(ItemStack stack) {
        RefinedGuState s = state(stack);
        if (!refined(stack)) return s.refineProgress() / (float) refineCost();
        return usesFedClock()
                ? fedLeft(stack) / (float) FED_BAR_UNITS
                : s.hunger() / (float) maxHunger();
    }
    //endregion

    //region the day clock
    public static boolean tickKept(ServerPlayer player, ItemStack stack, long days) {
        return tickOne(player, stack, days, true);
    }

    private static boolean tickOne(ServerPlayer player, ItemStack stack, long days, boolean kept) {
        if (!(stack.getItem() instanceof RefinableGuItem gu) || !gu.refined(stack)) return false;

        if (gu.usesFedClock()) {
            gu.payOwnUpkeep(player, stack);
            if (gu.fedAge(player, stack) >= DEATH_AFTER_TICKS) return true;
        } else if (days > 0L && gu.decay(player, stack, days)) {
            return true;
        }
        if (kept && gu.autoFeed(player, stack)) {
            if (gu.usesFedClock()) gu.refreshFedBar(player, stack);
            return false;
        }
        if (gu.usesFedClock()) gu.refreshFedBar(player, stack);
        if (gu.hungry(player, stack)) gu.warnHungry(player, stack);
        return false;
    }

    private void warnHungry(ServerPlayer player, ItemStack stack) {
        if (usesFedClock()) {
            warnOnceFed(player, stack);
        } else {
            announce(player, stack, MSG_HUNGRY);
        }
    }

    public static void starveAll(ServerPlayer player, long days) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (tickOne(player, stack, days, false)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                starved(player, stack);
            }
        }
    }

    public boolean decay(ServerPlayer player, ItemStack stack, long days) {
        RefinedGuState state = state(stack);
        int left = state.hunger() - (int) Math.min(days, maxHunger());
        if (left > 0) {
            store(stack, state.withHunger(left));
            return false;
        }
        if (player.hasInfiniteMaterials()) {
            store(stack, state.withHunger(0));
            return false;
        }
        return true;
    }

    public boolean autoFeed(ServerPlayer player, ItemStack stack) {
        if (usesFedClock()) return autoFeedMeal(player, stack);
        if (!hungry(player, stack)) return false;

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

            if (!player.hasInfiniteMaterials()) food.shrink((hunger * per + units - 1) / units);
            store(stack, state(stack).withHunger(state(stack).hunger() + hunger));
            ate = true;
        }
        return ate;
    }

    private boolean autoFeedMeal(ServerPlayer player, ItemStack stack) {
        if (!needsMeal(player, stack)) return false;

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (eatMeal(player, stack, inventory.getItem(slot))) return true;
        }
        return false;
    }

    public static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }

    public static void announceHungry(ServerPlayer player, ItemStack stack) {announce(player, stack, MSG_HUNGRY);}

    private static void died(ServerPlayer holder, ItemStack stack, String key) {
        announce(holder, stack, key);
        UUID uuid = owner(stack);
        if (uuid == null) return;

        ServerPlayer owner = holder.server.getPlayerList().getPlayer(uuid);
        if (owner != null) PlayerDataService.onVitalGuLost(owner, stack);
    }

    public static void starved(ServerPlayer holder, ItemStack stack) {died(holder, stack, MSG_STARVED);}
    public static void exhausted(ServerPlayer holder, ItemStack stack) {died(holder, stack, MSG_EXHAUSTED);}
    //endregion
}

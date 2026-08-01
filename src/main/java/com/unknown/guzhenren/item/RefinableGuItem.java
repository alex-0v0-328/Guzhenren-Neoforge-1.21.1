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

//  A Gu that is wild [野生] until refined, then fed and used: the Boar Gu, the Jin Strength Gu, and
//  anything shaped like them.
//  ⚠ It is NEVER consumed -- starving is its only end, so a capped owner can hand it to another player.
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

    //  ⚠⚠ The ONLY throttle left on refining since the per-hold cap went (2026-07-30). The FLOOR is on the
    //  essence he holds, not on what a step invests -- the last step legally pays less than this.
    protected int refineMinEssence() {return 20;}
    protected int maxHunger() {return 18;}
    public int usesPerGrant() {return 36;}
    //  ⚠⚠ ONE day of slack, FLAT for every Gu and every rank -- 「蛊饿了」 means "the next day rollover
    //  kills it", nothing earlier. ⚠ It must not scale with the meal: tied to mealDays, a Rank V vault
    //  warned 16 days out and repeated it daily. ⚠ 0 is unusable -- decay takes hunger 1 straight to
    //  death, so the bar never rests at 0 in survival and the warning would never fire at all.
    protected int hungryThreshold() {return 1;}

    //  What one use costs in hunger. ⚠ A Gu whose single use IS the whole meal bends this; it must still
    //  return at least 1, or the Gu could be driven forever and starvation would stop being its one end.
    protected int hungerPerUse(ItemStack stack) {return 1;}

    //  ⚠⚠ 0 is the ANSWER for a Gu whose use does not cultivate -- marks and specks are the player's own
    //  progress, so only a Gu that strengthens or alters him pays them. Not a placeholder.
    protected long speckPerUse() {return 0L;}

    //  炼化中 320 / 640 while wild, 使用中 12 / 36 once it answers to him -- the same two numbers the
    //  tooltip carries, so the bar never says something the item does not.
    @Override
    public Component chargeCaption(ItemStack stack) {
        RefinedGuState state = state(stack);
        return refined(stack)
                ? Component.translatable("guzhenren.hud.using", state.useCount(), usesPerGrant())
                : Component.translatable("guzhenren.hud.refining", state.refineProgress(), refineCost());
    }

    //  Which source those specks are booked under. A leaf whose branch owns a tag overrides this.
    protected MarkTag speckTag() {return MarkTag.NATURAL;}

    //  0-indexed rank offset, the exponent a rank-scaled ladder is built on (Rank I = 0).
    protected int tier() {return rank().ordinal() - Rank.ONE.ordinal();}

    //  base * factor^tier -- one ×N-per-rank ladder, shared by every rank-scaled leaf.
    protected static int scaled(int base, int factor, int tier) {
        int value = base;
        for (int i = 0; i < tier; i++) value *= factor;
        return value;
    }

    //  Holding right-click: three seconds at the Gu's own rank, one above it, nine below.  CLAUDE.md.
    protected int chargeTicks() {return 60;}
    protected int fastChargeTicks() {return 20;}
    protected int slowChargeTicks() {return 180;}

    //  ⚠ Units, not hunger: 4 units buy one hunger point. That is what lets one formula serve both
    //  "four pork = one hunger" (1 unit each) and "one raw iron = one hunger" (4 units each).
    protected int unitsPerHunger() {return 4;}
    //endregion

    //region the fed clock -- 酒虫 and 元老蛊 only
    //  ⚠⚠ ONE timeline, FLAT for every rank and every Gu that runs it: a meal buys two whole days of
    //  silence, the warning lands half a day later, death half a day after that. Only the MEAL ladders.
    //  His spec 2026-08-01. The two families that use it feed on a purpose, not on a daily nibble.
    public static final int FED_WINDOW_TICKS = 2 * Ticks.DAY;
    public static final int WARN_AFTER_TICKS = FED_WINDOW_TICKS + Ticks.HALF_DAY;
    public static final int DEATH_AFTER_TICKS = FED_WINDOW_TICKS + Ticks.DAY;

    //  The durability bar's resolution: 1000 ticks a unit, so a full life is 72 of them. ⚠ Coarse ON
    //  PURPOSE -- the bar is a cache the server publishes, and a coarser unit means it is rewritten once
    //  every fifty seconds instead of every one.
    public static final int FED_BAR_UNIT_TICKS = 1000;
    public static final int FED_BAR_UNITS = DEATH_AFTER_TICKS / FED_BAR_UNIT_TICKS;

    //  Whether this Gu runs the timestamp clock instead of the hunger-point one. ⚠ The two are exclusive:
    //  a fed-clock Gu never touches RefinedGuState.hunger, and decay() is not its clock.
    protected boolean usesFedClock() {return false;}

    //  What ONE meal costs, in items of its own food. ⚠ Both families ladder as base × 2^tier and nothing
    //  else -- 酒 8/16/32/64, 元石 2/4/8/16/32. The window above never moves.
    protected int mealItems() {return 0;}

    public static long fedAt(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FED_AT.get(), 0L);
    }

    //  ⚠ The OVERWORLD's day-time, the same clock aging reads -- so sleeping through a night starves a
    //  Gu exactly as it ages its owner, and a dimension's local time cannot skew it.
    public static long now(ServerPlayer player) {
        return player.server.overworld().getDayTime();
    }

    //  ⚠ Clamped at 0: /time set can move the clock backwards, and a negative age would read as freshly fed.
    private static long fedAge(ServerPlayer player, ItemStack stack) {
        return Math.max(0L, now(player) - fedAt(stack));
    }

    //  Stamps it fed. ⚠ The ONLY write this clock has -- there is no bar to fill and nothing to decay,
    //  and rewriting FED_AT un-latches the warning for free.
    protected void stampFed(ServerPlayer player, ItemStack stack) {
        stack.set(ModDataComponents.FED_AT.get(), now(player));
        //  ⚠ The bar jumps to full HERE rather than waiting for the next refresh -- feeding is a click he
        //  is looking at, and a bar that catches up a second later reads as the feed not having landed.
        stack.set(ModDataComponents.FED_LEFT.get(), FED_BAR_UNITS);
    }

    //  Whether the meal has run out and it is time to buy another -- BEFORE the warning, let alone death.
    protected boolean needsMeal(ServerPlayer player, ItemStack stack) {
        return usesFedClock() && refined(stack) && fedAge(player, stack) >= FED_WINDOW_TICKS;
    }

    //  What the bar shows: units left before it starves, 0..FED_BAR_UNITS.
    public static int fedLeft(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FED_LEFT.get(), FED_BAR_UNITS);
    }

    //  ⚠ Writes only on a CHANGE. The clock is read every second; the bar moves every fifty, and a write
    //  each second would re-sync the stack to the client that often, per Gu.
    private void refreshFedBar(ServerPlayer player, ItemStack stack) {
        long left = (DEATH_AFTER_TICKS - fedAge(player, stack)) / FED_BAR_UNIT_TICKS;
        int units = (int) Math.clamp(left, 0L, FED_BAR_UNITS);
        if (units != fedLeft(stack)) stack.set(ModDataComponents.FED_LEFT.get(), units);
    }

    //  A Gu with a larder of its OWN pays here, on every heartbeat and before the clock is read.
    //  ⚠ Must no-op when no meal is due -- it runs once a second. The Primeval Elder Gu's vault is the
    //  only one today; the other hand and the player's pack are NOT larders, they are feeding.
    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {}

    //  ⚠ Speaks at most ONCE per meal. Read every second, so without the latch it would repeat every
    //  second for the last half day of the Gu's life.
    private void warnOnceFed(ServerPlayer player, ItemStack stack) {
        long fed = fedAt(stack);
        if (stack.getOrDefault(ModDataComponents.FED_WARNED.get(), Long.MIN_VALUE) == fed) return;

        stack.set(ModDataComponents.FED_WARNED.get(), fed);
        announce(player, stack, MSG_HUNGRY);
    }
    //endregion

    //region what a leaf must answer
    //  Units one item of this food is worth here; 0 means it is not food for this Gu.
    protected abstract int feedUnits(ItemStack food);

    //  Why this player cannot use it right now -- typically "you already hold what it grants".
    //  ⚠ Takes the stack, as gate() does: what a Gu carrying its own store can hand out depends on it.
    protected abstract @Nullable Refusal payoutGate(Player player, ItemStack stack);

    //  Refill the bar from whatever larder this Gu has, after the hunger write and BEFORE the hungry
    //  warning. ⚠⚠ Never after apply() returns -- the warning fires inside, so a later top-up makes
    //  every single use announce 「蛊饿了」 on a bar about to be full again.
    //  ⚠ The base's larder is the OTHER HAND: 蹲+右键+食物 must end on a FULL bar, so the point the use
    //  just spent is paid by the same food. A plain use holds no food, so this is a no-op there.
    protected void afterUse(ServerPlayer player, ItemStack stack) {eat(player, stack);}

    //  What the 36th use buys. Runs once per completed cycle.
    //  ⚠ It may write the stack's OWN components, never its RefinedGuState -- apply() holds a copy of that
    //  and stores it afterwards, so a hunger or use-count write here would be silently clobbered.
    protected abstract void payout(ServerPlayer player, ItemStack stack);
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

    //  What "the Gu is hungry" means -- ONE question, answered by whichever clock this Gu runs.
    //  ⚠ Starvation is not asked, it is reported: tickKept returns it, because only the biller knows.
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

    //  ⚠ No cooldown override any more: the CHARGE is the pacing now. A one-second cooldown on top of a
    //  four-second hold only broke the bar into stutters while he kept the button down.
    //  ⚠ Both the refine and the use are charged, so this is what the hotbar bar counts down. The
    //  comparison is rank against rank -- a bigger stage buys nothing here.
    //  ⚠ slowChargeTicks was unreachable until the Liquor Worm [酒虫] series: it runs ranks I..IV, so a
    //  Rank I cultivator refining a Rank IV worm finally stands BELOW one. It is live -- do not prune it.
    @Override
    protected int useDurationTicks(Player player, ItemStack stack) {
        //  ⚠⚠ Feeding is not driving, so the pure-feed click is INSTANT. Every click that uses the Gu
        //  charges, and all of them for the same length -- which is what lets the sneak action charge.
        if (holdingFood(player, stack) && !player.isShiftKeyDown()) return 0;
        int gap = rankGap(player);
        if (gap > 0) return fastChargeTicks();
        return gap == 0 ? chargeTicks() : slowChargeTicks();
    }

    //  How many ranks he stands above this Gu; negative means below it. ⚠ Since the per-hold cap went
    //  (2026-07-30) this feeds the charge time ALONE -- a bigger 转数 buys speed and nothing else.
    private int rankGap(Player player) {
        return ApertureService.rank(player).ordinal() - rank().ordinal();
    }

    //  What refuses BOTH clicks. ⚠ A mortal drives no Gu at all, refined or not -- an aperture is what
    //  pushes one; and below 20 essence a wild Gu is refused outright rather than refined by trickle.
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

        //  ⚠ With food in hand this click only FEEDS, so a payout he cannot receive must not stop him.
        //  ⚠ A full bar is NOT refused: every gesture already has an outcome, so nothing needs saying.
        if (holdingFood(player, stack)) return null;
        //  ⚠ Hunger is NOT a refusal any more: an empty Gu may always be forced, and dies of it.
        //  See apply() and CLAUDE.md "The refinable Gu".
        return payoutGate(player, stack);
    }

    @Override
    protected int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) {
            refineStep(player, stack);
            return 0;
        }
        //  ⚠⚠ Food in the other hand makes the plain click a FEED and nothing else (2026-07-31, his
        //  spec). Crouching is what says "use it anyway" -- one click used to do both.
        if (holdingFood(player, stack)) {
            eat(player, stack);
            return 0;
        }
        return drive(player, stack);
    }

    //  The use itself, shared by the plain click and the crouching feed-and-use.
    //  ⚠ A leaf that spends its sneak click on something else calls THIS, never super.apply -- that one
    //  now feeds when its own food is in the other hand (元老蛊 and 元石 are exactly that pair).
    protected int drive(ServerPlayer player, ItemStack stack) {
        //  ⚠ Read after any feeding: food in the other hand still rescues a Gu about to be forced.
        boolean forced = state(stack).hunger() <= 0;

        RefinedGuState state = state(stack);
        state = state.withUses(state.useCount() + 1)
                .withHunger(state.hunger() - Math.max(1, hungerPerUse(stack)));
        if (speckPerUse() > 0) PathService.addSpeck(player, path(), speckTag(), speckPerUse());

        //  The 36th pays out and the count starts over -- useCount is progress toward the NEXT payout.
        if (state.useCount() >= usesPerGrant()) {
            payout(player, stack);
            state = state.withUses(0);
        }
        store(stack, state);

        //  ⚠⚠ The forced use LANDS first -- speck, count, payout -- then it dies. Still starvation, NOT a
        //  payout; this 1 is a death and must never be gated on "spent".  CLAUDE.md "The refinable Gu".
        //  ⚠ Creative never loses the item (GuItem.spend), so it must not be told the Gu died either.
        if (forced && !player.hasInfiniteMaterials()) {
            exhausted(player, stack);
            return 1;
        }
        //  ⚠⚠ A Gu that pays its own upkeep refills the bar HERE, BEFORE the warning -- otherwise it
        //  announces hunger on a bar it is about to top back up. This is the same order decay() uses.
        afterUse(player, stack);
        //  The warning has to land on the USE too -- the day tick alone would stay silent through a
        //  whole session of clicking it down from 9. ⚠ A fed-clock Gu latches instead, or a session of
        //  withdrawals would repeat it; the heartbeat says it either way within a second.
        if (!hungry(player, stack)) return 0;
        if (usesFedClock()) {
            warnOnceFed(player, stack);
        } else {
            announceHungry(player, stack);
        }
        return 0;
    }

    //  ⚠⚠ 蹲+右键 with food is FEED-THEN-USE (2026-07-31): the plain click having become a pure feed,
    //  crouching is now the gesture that says "use it anyway". A full bar simply gives eat() nothing.
    //  ⚠⚠ Answering false is what lets a CROUCHING player still refine and still use: with no food in
    //  the other hand, sneak+right-click is simply the ordinary right-click.
    @Override
    protected boolean hasSneakUse(Player player, ItemStack stack) {return holdingFood(player, stack);}

    //  ⚠ Reached only with food in hand, and it USES the Gu -- so a full bar is fine here, unlike gate().
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

    //  Whether the other hand holds this Gu's food AT ALL. ⚠⚠ NOT "can it eat right now": a full bar is
    //  still food, and collapsing the two is what made a crouching feed silently use the Gu instead.
    //  ⚠ Not `feedable()` either, which is the boolean axis saying whether this kind is fed at all.
    private boolean holdingFood(Player player, ItemStack stack) {
        return refined(stack) && feedUnits(player.getOffhandItem()) > 0;
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

    //  One WHOLE meal or nothing -- a fed-clock Gu has no bar to part-fill, so a partial payment would
    //  buy nothing and vanish. ⚠ Refuses early while the last meal still runs, or an eager click would
    //  burn a full meal to reset a clock that had two days left on it.
    //  Returns true if it ate. ⚠ Creative pays no food here either.
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
    //  ⚠⚠ Invest EVERYTHING he can spare -- no refinable Gu caps a hold any more (2026-07-30), so how many
    //  holds a refine takes is his POOL's answer, not a constant. Do not bring back a refinePerUse.
    private void refineStep(ServerPlayer player, ItemStack stack) {
        RefinedGuState state = state(stack);
        int remaining = refineCost() - state.refineProgress();
        //  ⚠ spendable(), not currentEssence: a distilling cultivator's ordinary pool is 0 by design, and
        //  his distilled points are worth two apiece -- refining is exactly what that discount is for.
        int invest = (int) Math.min(remaining, EssenceService.spendable(player));
        if (invest <= 0) return;

        EssenceService.consume(player, invest);
        int next = state.refineProgress() + invest;

        //  ⚠ Completing it hands back a FULL Gu (it was half until 2026-07-27) -- the feeding clock
        //  starts at the top, so the first thing a new owner does is use it, not feed it.
        boolean done = next >= refineCost();
        store(stack, done ? new RefinedGuState(refineCost(), 0, maxHunger()) : state.withRefine(next));

        //  ⚠⚠ A fed-clock Gu MUST be stamped here: FED_AT defaults to 0 against a world clock in the
        //  millions, so an unstamped one reads as three days overdue and dies the instant it is refined.
        if (done && usesFedClock()) stampFed(player, stack);
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
        //  ⚠ Never both -- one line, whichever half of its life it is in.
        tooltip.add(progressLine(stack).withStyle(ChatFormatting.GRAY));
    }

    //  Wild reads how far along the refining is; refined reads how far along the cycle is.
    //  A leaf whose cycle counts to one replaces the refined half -- "已用 0/1" would say nothing.
    protected MutableComponent progressLine(ItemStack stack) {
        return refined(stack)
                ? Component.translatable(TOOLTIP_USES, state(stack).useCount(), usesPerGrant())
                : Component.translatable(TOOLTIP_REFINE, state(stack).refineProgress(), refineCost());
    }

    //  Wild: how far refined. Refined: how fed. One bar, two meanings -- the name says which you read.
    //  ⚠ A fed-clock Gu reads the published FED_LEFT rather than its own clock: a bar method is handed
    //  nothing but the stack, so it cannot see the world time. The server writes the answer down.
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
    //  ⚠⚠ ONE day's billing for a KEPT Gu -- the aperture store, the Vital slot and CustomPlayer's body
    //  parts all call THIS, so "food means it never starves" is one behaviour and not three copies of it.
    //  Decay, then feed it from his inventory, then warn if nothing could be fed.
    //  ⚠ Returns whether it starved and does NOT empty anything: only the container can clear its own
    //  slot, and only it knows how. The caller owes starved() too, which is what bills a Vital Gu's owner.
    //  ⚠ The player's INVENTORY deliberately does not use this -- carrying a Gu never auto-feeds it, see
    //  starveAll below. Putting one inside the aperture is what buys the feeding.
    public static boolean tickKept(ServerPlayer player, ItemStack stack, long days) {
        return tickOne(player, stack, days, true);
    }

    //  ⚠⚠ THE one detection. Four containers reach it -- inventory, aperture store, Vital slot, body
    //  parts -- and it serves BOTH clocks: a timestamp is read every heartbeat and ignores `days`, a
    //  hunger bar is billed and only moves when a day rolled over. Do not split it back apart.
    //  `kept` is the one difference between them: only a kept Gu is auto-fed from his pack.
    private static boolean tickOne(ServerPlayer player, ItemStack stack, long days, boolean kept) {
        if (!(stack.getItem() instanceof RefinableGuItem gu) || !gu.refined(stack)) return false;

        if (gu.usesFedClock()) {
            //  ⚠ Its OWN larder pays first, wherever it is carried -- that is what lets a stocked
            //  Primeval Elder Gu [元老蛊] outlast any absence even in a plain inventory slot.
            gu.payOwnUpkeep(player, stack);
            if (gu.fedAge(player, stack) >= DEATH_AFTER_TICKS) return true;
        } else if (days > 0L && gu.decay(player, stack, days)) {
            return true;
        }
        //  ⚠ Feeding comes after billing on purpose: "food means it never starves" is the NET effect of
        //  the two, never a skipped bill. Same rule the hand-fed path obeys.
        if (kept && gu.autoFeed(player, stack)) {
            if (gu.usesFedClock()) gu.refreshFedBar(player, stack);
            return false;
        }
        if (gu.usesFedClock()) gu.refreshFedBar(player, stack);
        if (gu.hungry(player, stack)) gu.warnHungry(player, stack);
        return false;
    }

    //  ⚠ The fed clock latches; the hunger bar speaks once per rollover and needs none.
    private void warnHungry(ServerPlayer player, ItemStack stack) {
        if (usesFedClock()) {
            warnOnceFed(player, stack);
        } else {
            announce(player, stack, MSG_HUNGRY);
        }
    }

    //  ⚠ ONE walk for every refinable Gu, so a new one needs no line anywhere. The old per-class statics
    //  each needed their own call in PlayerTickEvents, and forgetting it meant that Gu never starved.
    //  ⚠⚠ CARRIED, not kept: the inventory never auto-feeds. Putting a Gu inside the aperture is what
    //  buys that -- his call, long standing. A fed-clock Gu therefore only ever starves out here.
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

    //  Bills the elapsed days against one Gu. True means it starved. ⚠ days can be far over 1.
    //  ⚠ Creative starves it DOWN but never past: hunger still falls and still warns, the death simply
    //  does not land -- exactly as lifespan drains to 0 in creative without killing (checkLethalState).
    //  ⚠ The one guard for every death by hunger; the forced use carries the matching one in apply().
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

    //  Feeds one stored Gu out of the player's own inventory, once it is hungry.
    //  ⚠ Decay has already been billed by the caller, so "food means no hunger lost" is the NET effect,
    //  not a skipped tick -- the rule stays identical to hand-feeding, only the hand is automatic.
    //  Returns true if anything was eaten. ⚠ Scans the inventory only; Curios slots are not covered.
    public boolean autoFeed(ServerPlayer player, ItemStack stack) {
        //  ⚠ A fed-clock Gu buys a WHOLE meal the moment its window lapses -- earlier than the warning,
        //  so a stocked player never hears one. Whole meals only; there is no bar to part-fill.
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

            //  ⚠ Creative pays no food here either -- eat() has carried this guard all along and this
            //  walk did not, which left the same rule wearing two faces.
            if (!player.hasInfiniteMaterials()) food.shrink((hunger * per + units - 1) / units);
            store(stack, state(stack).withHunger(state(stack).hunger() + hunger));
            ate = true;
        }
        return ate;
    }

    //  One meal out of his pack, whole or not at all. ⚠ Walks for a slot holding ENOUGH -- a meal cannot
    //  be gathered across two stacks, which is what keeps "one meal, one payment" true.
    private boolean autoFeedMeal(ServerPlayer player, ItemStack stack) {
        if (!needsMeal(player, stack)) return false;

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (eatMeal(player, stack, inventory.getItem(slot))) return true;
        }
        return false;
    }

    //  Plain chat, uncolored: nothing was refused, and the action bar gets missed while he is busy.
    public static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }

    public static void announceHungry(ServerPlayer player, ItemStack stack) {announce(player, stack, MSG_HUNGRY);}

    //  Its life ended. Whoever was carrying it hears so; if it was someone's Vital Gu, its OWNER pays --
    //  which is why the mark stores a UUID and not a flag. ⚠ An offline owner is not billed and nothing
    //  is queued: this mod has no pending-penalty concept, and one case does not justify inventing it.
    private static void died(ServerPlayer holder, ItemStack stack, String key) {
        announce(holder, stack, key);
        UUID uuid = owner(stack);
        if (uuid == null) return;

        ServerPlayer owner = holder.server.getPlayerList().getPlayer(uuid);
        if (owner != null) PlayerDataService.onVitalGuLost(owner, stack);
    }

    //  ⚠ Two deaths, one funnel, two messages -- neglect and force are not the same story to tell.
    public static void starved(ServerPlayer holder, ItemStack stack) {died(holder, stack, MSG_STARVED);}
    public static void exhausted(ServerPlayer holder, ItemStack stack) {died(holder, stack, MSG_EXHAUSTED);}
    //endregion
}

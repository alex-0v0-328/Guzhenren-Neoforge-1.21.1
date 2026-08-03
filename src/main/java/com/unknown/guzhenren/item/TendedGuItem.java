package com.unknown.guzhenren.item;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.display.ModDisplayText;
import com.unknown.guzhenren.item.material.PrimevalStoneItem;
import com.unknown.guzhenren.registry.ModDataComponents;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TendedGuItem extends MortalGuItem {

    private static final String TOOLTIP_REFINE = "guzhenren.item.gu.refine_progress";
    private static final String TOOLTIP_INVESTED = "guzhenren.item.gu.invested";
    private static final String TOOLTIP_HUNGER = "guzhenren.item.gu.hunger_progress";
    private static final String CAPTION_CHANNELING = "guzhenren.hud.using";
    private static final String MSG_HUNGRY = "guzhenren.item.gu.hungry";
    private static final String MSG_STARVED = "guzhenren.item.gu.starved";
    private static final String MSG_EXHAUSTED = "guzhenren.item.gu.exhausted";

    private static final int ESSENCE_FLOOR = 20;

    protected final GuClock clock;

    protected TendedGuItem(Properties properties, GuSpec spec) {
        super(properties, spec);
        this.clock = spec.buildClock();
    }

    //region what a leaf must answer
    protected abstract @Nullable Refusal payoutGate(Player player, ItemStack stack);
    protected abstract void payout(ServerPlayer player, ItemStack stack);
    protected int feedUnits(ItemStack food) {return spec.feedUnits(food);}
    //endregion

    //region state
    public static RefinedGuState state(ItemStack s) {
        return s.getOrDefault(ModDataComponents.REFINED_GU_STATE.get(), RefinedGuState.WILD);
    }

    private void store(ItemStack stack, RefinedGuState s) {
        stack.set(ModDataComponents.REFINED_GU_STATE.get(), new RefinedGuState(s.refined(),
                Math.min(s.refineProgress(), refineCost()),
                Math.min(s.investedEssence(), spec.essencePerRound()),
                s.hunger()));
    }

    public boolean refined(ItemStack stack) {return state(stack).refined();}
    public boolean hungry(ServerPlayer p, ItemStack s) {return refined(s) && clock.hungry(p, s);}
    //endregion

    //region refining [炼化] -- paid in instalments; what it buys is a lasting bond
    public static final int REFINE_DONE_COOLDOWN_TICKS = Ticks.SECOND;

    private void refineStep(ServerPlayer player, ItemStack stack) {
        RefinedGuState s = state(stack);
        int invest = (int) Math.min(refineCost() - s.refineProgress(), EssenceService.spendable(player));
        if (invest > 0) EssenceService.consume(player, invest);

        int next = s.refineProgress() + invest;
        if (next < refineCost()) {
            store(stack, s.withRefine(next));
            return;
        }
        store(stack, new RefinedGuState(true, refineCost(), 0, 0));
        clock.bind(player, stack);
        stack.set(ModDataComponents.REFINED_AT.get(), gameTime(player));
    }
    //endregion

    //region channeling [灌注] -- Human Jun [人力钧力流] and the boars
    private static final int CHANNEL_MAX_TICKS = 72_000;
    private static final int CHANNEL_STEP_TICKS = 5;
    private static final int CHANNEL_STEPS = Ticks.SECOND / CHANNEL_STEP_TICKS;

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack,
                          int remaining) {
        if (!spec.channels() || !refined(stack) || !(entity instanceof ServerPlayer player)) return;

        int elapsed = CHANNEL_MAX_TICKS - remaining;
        if (elapsed < 0 || elapsed % CHANNEL_STEP_TICKS != 0) return;
        if (elapsed == 0 && crouching(player) && holdingFood(player, stack)) eat(player, stack);

        drawFromOffhandStones(player);
        int take = stepAmount(player, stack, (elapsed % Ticks.SECOND) / CHANNEL_STEP_TICKS);
        if (take <= 0 || !EssenceService.consume(player, take)) {
            player.stopUsingItem();
            return;
        }
        pour(player, stack, take);
    }

    private void drawFromOffhandStones(ServerPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof PrimevalStoneItem stone)) return;
        if (EssenceService.currentEssence(player) >= EssenceService.maxEssence(player)) return;

        EssenceService.add(player, stone.essence());
        if (!player.hasInfiniteMaterials()) offhand.shrink(1);
    }

    private int stepAmount(ServerPlayer player, ItemStack stack, int stepIndex) {
        long pool = EssenceService.spendable(player);
        if (pool < ESSENCE_FLOOR) return 0;

        int round = spec.essencePerRound();
        int poolShareThisStep = (int) (pool / (CHANNEL_STEPS + 1 - stepIndex));
        int leftInThisRound = round - state(stack).investedEssence() % round;
        int mostTheBarCanCarry = clock.affordable(stack);

        return Math.min(Math.min(poolShareThisStep, leftInThisRound), mostTheBarCanCarry);
    }

    private void pour(ServerPlayer player, ItemStack stack, int amount) {
        int pouredBefore = state(stack).investedEssence();
        int pouredAfter = pouredBefore + amount;

        clock.pour(stack, pouredBefore, pouredAfter);
        paySpecksCrossed(player, pouredBefore, pouredAfter);
        store(stack, state(stack).withInvested(pouredAfter));

        if (pouredAfter >= spec.essencePerRound()) {
            grant(player, stack);
            store(stack, state(stack).withInvested(0));
            if (payoutGate(player, stack) != null) player.stopUsingItem();
        }
        if (clock.hungry(player, stack)) announceHungry(player, stack);
        if (clock.affordable(stack) <= 0) player.stopUsingItem();
    }

    private void paySpecksCrossed(ServerPlayer player, int pouredBefore, int pouredAfter) {
        int per = spec.essencePerSpeck();
        if (per <= 0) return;

        long crossed = pouredAfter / per - pouredBefore / per;
        if (crossed > 0) PathService.addSpeck(player, path(), spec.speckTag(), crossed);
    }

    private void grant(ServerPlayer player, ItemStack stack) {
        payout(player, stack);
        if (spec.useCooldownTicks() > 0) stack.set(ModDataComponents.USED_AT.get(), gameTime(player));
    }
    //endregion

    //region the long cooldown -- vanilla draws the sweep, the stack's stamp is the truth
    private static final String FAILED_COOLDOWN = "guzhenren.item.failed.gu_cooldown";

    private static long gameTime(ServerPlayer player) {return player.server.overworld().getGameTime();}

    private int cooldownLeft(Player player, @Nullable Long stamp, int window) {
        MinecraftServer server = player.getServer();
        if (stamp == null || server == null || window <= 0) return 0;

        long elapsed = server.overworld().getGameTime() - stamp;
        return elapsed < 0L || elapsed >= window ? 0 : (int) (window - elapsed);
    }

    private int useCooldownLeft(Player p, ItemStack s) {
        return cooldownLeft(p, s.get(ModDataComponents.USED_AT.get()), spec.useCooldownTicks());
    }

    private @Nullable Refusal cooldownRefusal(Player player, ItemStack stack) {
        int left = useCooldownLeft(player, stack);
        if (left <= 0) return null;

        if (player instanceof ServerPlayer server) server.getCooldowns().addCooldown(this, left);
        long seconds = (left + Ticks.SECOND - 1) / Ticks.SECOND;
        return new Refusal(FAILED_COOLDOWN, Component.literal(String.valueOf(seconds)));
    }

    @Override
    protected void spend(ServerPlayer player, ItemStack stack, int count) {
        super.spend(player, stack, count);
        int left = Math.max(useCooldownLeft(player, stack),
                cooldownLeft(player, stack.get(ModDataComponents.REFINED_AT.get()), REFINE_DONE_COOLDOWN_TICKS));
        if (left > 0) player.getCooldowns().addCooldown(this, left);
    }
    //endregion

    //region the clicks
    @Override
    protected boolean feedsFromOffhand() {return true;}

    @Override
    protected final @Nullable Refusal gate(Player player, ItemStack stack) {
        if (holdingFood(player, stack)) return null;
        if (!refined(stack)) {
            return essenceGate(player, Math.min(ESSENCE_FLOOR, refineCost()), FAILED_REFINE_ESSENCE);
        }
        Refusal poor = essenceGate(player, useThreshold(stack), FAILED_ESSENCE);
        if (poor != null) return poor;

        Refusal cooling = cooldownRefusal(player, stack);
        return cooling != null ? cooling : useGate(player, stack);
    }

    protected long useThreshold(ItemStack stack) {
        return spec.channels() ? ESSENCE_FLOOR : spec.essencePerRound();
    }

    @Override
    protected @Nullable Refusal useGate(Player player, ItemStack stack) {return payoutGate(player, stack);}

    @Override
    protected final int apply(ServerPlayer player, ItemStack stack) {
        if (!refined(stack)) {
            refineStep(player, stack);
            return 0;
        }
        if (holdingFood(player, stack) && !crouching(player)) {
            eat(player, stack);
            return 0;
        }
        return useApply(player, stack);
    }

    @Override
    protected int useApply(ServerPlayer player, ItemStack stack) {
        return spec.channels() ? 0 : drive(player, stack);
    }

    @Override
    protected final int useDurationTicks(Player player, ItemStack stack) {
        if (!refined(stack)) return chargeTicksFor(player);
        if (holdingFood(player, stack) && !crouching(player)) return 0;
        return spec.channels() ? CHANNEL_MAX_TICKS : useChargeTicks(player, stack);
    }

    protected int useChargeTicks(Player player, ItemStack stack) {return chargeTicksFor(player);}

    private int chargeTicksFor(Player player) {
        int gap = ApertureService.rank(player).ordinal() - rank().ordinal();
        if (gap > 0) return spec.fastChargeTicks();
        return gap == 0 ? spec.sameChargeTicks() : spec.slowChargeTicks();
    }

    protected int drive(ServerPlayer player, ItemStack stack) {
        EssenceService.consume(player, spec.essencePerRound());
        boolean drivenOnAnEmptyBar = clock.spendOnce(stack);
        paySpecksCrossed(player, 0, spec.essencePerRound());
        grant(player, stack);

        if (drivenOnAnEmptyBar && !player.hasInfiniteMaterials()) {
            exhausted(player, stack);
            return 1;
        }
        eat(player, stack);
        if (hungry(player, stack)) clock.warn(player, stack, 1L);
        return 0;
    }

    @Override
    protected boolean hasSneakUse(Player player, ItemStack stack) {return holdingFood(player, stack);}

    @Override
    protected @Nullable Refusal sneakGate(Player player, ItemStack stack) {
        Refusal poor = essenceGate(player, useThreshold(stack), FAILED_ESSENCE);
        if (poor != null) return poor;

        Refusal cooling = cooldownRefusal(player, stack);
        return cooling != null ? cooling : payoutGate(player, stack);
    }

    @Override
    protected int sneakApply(ServerPlayer player, ItemStack stack) {
        eat(player, stack);
        return spec.channels() ? 0 : drive(player, stack);
    }

    protected boolean holdingFood(Player player, ItemStack stack) {
        return refined(stack) && feedUnits(player.getOffhandItem()) > 0;
    }

    protected void eat(ServerPlayer player, ItemStack stack) {
        clock.eat(this, player, stack, player.getOffhandItem());
    }
    //endregion

    //region display
    @Override
    public Component chargeCaption(ItemStack stack, int remainingTicks) {
        if (!refined(stack)) return refineCaption(state(stack).refineProgress());
        return spec.channels()
                ? Component.translatable(CAPTION_CHANNELING, state(stack).investedEssence(),
                        spec.essencePerRound())
                : Component.translatable(CAPTION_USING_PLAIN);
    }

    @Override
    public @Nullable Float chargeFraction(ItemStack stack, int remainingTicks) {
        if (!spec.channels() || !refined(stack)) return null;
        return state(stack).investedEssence() / (float) spec.essencePerRound();
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return refined(stack) ? super.getName(stack) : ModDisplayText.wild(super.getName(stack));
    }

    @Override
    protected @Nullable MutableComponent progressLine(ItemStack stack) {
        if (!refined(stack)) {
            return Component.translatable(TOOLTIP_REFINE, state(stack).refineProgress(), refineCost());
        }
        if (spec.channels()) {
            return Component.translatable(TOOLTIP_INVESTED, state(stack).investedEssence(),
                    spec.essencePerRound());
        }
        return clock instanceof GuClock.HungerBar bar
                ? Component.translatable(TOOLTIP_HUNGER, state(stack).hunger(), bar.max())
                : null;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return refined(stack) ? clock.barVisible(stack) : state(stack).refineProgress() > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {return Math.round(barFraction(stack) * 13.0F);}

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return Mth.hsvToRgb(barFraction(stack) / 3.0F, 1.0F, 1.0F);
    }

    private float barFraction(ItemStack stack) {
        if (refined(stack)) return clock.barFraction(stack);
        return refineCost() <= 0 ? 0.0F : state(stack).refineProgress() / (float) refineCost();
    }
    //endregion

    //region the day clock -- three containers bill through here, on either clock
    public static boolean tickInContainer(ServerPlayer player, ItemStack stack, long days) {
        return tickOne(player, stack, days, true);
    }

    private static boolean tickOne(ServerPlayer player, ItemStack stack, long days, boolean kept) {
        if (!(stack.getItem() instanceof TendedGuItem gu) || !gu.refined(stack)) return false;

        gu.payOwnUpkeep(player, stack);
        if (gu.clock.tick(player, stack, days)) return true;

        if (kept && gu.autoFeed(player, stack)) return false;
        if (gu.clock.hungry(player, stack)) gu.clock.warn(player, stack, days);
        return false;
    }

    protected void payOwnUpkeep(ServerPlayer player, ItemStack stack) {}

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

    public boolean autoFeed(ServerPlayer player, ItemStack stack) {
        if (!clock.hungry(player, stack)) return false;

        Inventory inventory = player.getInventory();
        boolean ate = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (clock.eat(this, player, stack, inventory.getItem(slot))) ate = true;
        }
        return ate;
    }
    //endregion

    //region the two deaths -- one funnel, two messages, and a Vital Gu's owner pays either way
    public static void announce(ServerPlayer player, ItemStack stack, String key) {
        player.sendSystemMessage(Component.translatable(key, stack.getHoverName()));
    }

    public static void announceHungry(ServerPlayer p, ItemStack s) {announce(p, s, MSG_HUNGRY);}

    private static void died(ServerPlayer holder, ItemStack stack, String key) {
        announce(holder, stack, key);
        UUID uuid = owner(stack);
        if (uuid == null) return;

        ServerPlayer owner = holder.server.getPlayerList().getPlayer(uuid);
        if (owner != null) PlayerDataService.onVitalGuLost(owner, stack);
    }

    public static void starved(ServerPlayer holder, ItemStack s) {died(holder, s, MSG_STARVED);}
    public static void exhausted(ServerPlayer holder, ItemStack s) {died(holder, s, MSG_EXHAUSTED);}
    //endregion
}

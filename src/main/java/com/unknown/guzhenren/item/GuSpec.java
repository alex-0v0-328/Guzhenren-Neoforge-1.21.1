package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class GuSpec {

    public static final int FAST_CHARGE_TICKS = 20;
    public static final int SAME_CHARGE_TICKS = 60;
    public static final int SLOW_CHARGE_TICKS = 180;

    private final Rank rank;
    private final GuPath path;

    private int refineCost;

    private int essencePerRound;
    private boolean channels;
    private int essencePerSpeck;
    private MarkTag speckTag = MarkTag.NATURAL;

    private int maxHunger;
    private int unitsPerHunger = 1;
    private int essencePerHunger;
    private int hungerPerUse = 1;
    private int mealItems;

    private @Nullable TagKey<Item> feedTag;
    private int feedUnits;
    private @Nullable TagKey<Item> denseTag;
    private int denseUnits;

    private int useCooldownTicks;
    private int fastChargeTicks = FAST_CHARGE_TICKS;
    private int sameChargeTicks = SAME_CHARGE_TICKS;
    private int slowChargeTicks = SLOW_CHARGE_TICKS;

    private GuSpec(Rank rank, GuPath path) {
        this.rank = rank;
        this.path = path;
    }

    public static GuSpec of(Rank rank, GuPath path) {return new GuSpec(rank, path);}

    //region the chain -- one call per fact, so a registration reads as a table row
    public GuSpec refine(int cost) {
        this.refineCost = cost;
        return this;
    }

    public GuSpec channel(int essencePerRound) {
        this.essencePerRound = essencePerRound;
        this.channels = true;
        return this;
    }

    public GuSpec costPerUse(int essence) {
        this.essencePerRound = essence;
        this.channels = false;
        return this;
    }

    public GuSpec speckEvery(int essence, MarkTag tag) {
        this.essencePerSpeck = essence;
        this.speckTag = tag;
        return this;
    }

    public GuSpec hungerBar(int max, int unitsPerHunger) {
        this.maxHunger = max;
        this.unitsPerHunger = unitsPerHunger;
        return this;
    }

    public GuSpec hungerEvery(int essence) {
        this.essencePerHunger = essence;
        return this;
    }

    public GuSpec hungerPerUse(int points) {
        this.hungerPerUse = points;
        return this;
    }

    public GuSpec fedClock(int mealItems) {
        this.mealItems = mealItems;
        return this;
    }

    public GuSpec feed(TagKey<Item> tag, int units) {
        this.feedTag = tag;
        this.feedUnits = units;
        return this;
    }

    public GuSpec dense(TagKey<Item> tag, int units) {
        this.denseTag = tag;
        this.denseUnits = units;
        return this;
    }

    public GuSpec cooldown(int ticks) {
        this.useCooldownTicks = ticks;
        return this;
    }

    public GuSpec charge(int fast, int same, int slow) {
        this.fastChargeTicks = fast;
        this.sameChargeTicks = same;
        this.slowChargeTicks = slow;
        return this;
    }
    //endregion

    //region what the base classes read
    public Rank rank() {return rank;}
    public GuPath path() {return path;}
    public int refineCost() {return refineCost;}
    public int essencePerRound() {return essencePerRound;}
    public boolean channels() {return channels;}
    public int essencePerSpeck() {return essencePerSpeck;}
    public MarkTag speckTag() {return speckTag;}
    public int mealItems() {return mealItems;}
    public int useCooldownTicks() {return useCooldownTicks;}
    public int fastChargeTicks() {return fastChargeTicks;}
    public int sameChargeTicks() {return sameChargeTicks;}
    public int slowChargeTicks() {return slowChargeTicks;}

    public GuClock buildClock() {
        if (mealItems > 0) return new GuClock.FedClock(mealItems);
        if (maxHunger > 0) {
            return new GuClock.HungerBar(maxHunger, unitsPerHunger, essencePerHunger, hungerPerUse);
        }
        return new GuClock.NoClock();
    }

    public int feedUnits(ItemStack food) {
        if (denseTag != null && food.is(denseTag)) return denseUnits;
        return feedTag != null && food.is(feedTag) ? feedUnits : 0;
    }
    //endregion

    //region the startup check -- a typo here should never reach the game
    public void validate(String id) {
        if (refineCost < 0) throw fault(id, "refine cost is negative");
        if (essencePerRound < 0) throw fault(id, "essence per round is negative");
        if (mealItems > 0 && maxHunger > 0) throw fault(id, "declares both a fed clock and a hunger bar");

        if (!channels) return;

        if (essencePerRound <= 0) throw fault(id, "channels but pours nothing");
        if (essencePerSpeck <= 0) throw fault(id, "channels but pays no specks");
        if (essencePerHunger <= 0) throw fault(id, "channels but costs no hunger");
        if (essencePerRound % essencePerSpeck != 0) {
            throw fault(id, "round %d does not divide by speck rate %d"
                    .formatted(essencePerRound, essencePerSpeck));
        }
        if (essencePerRound % essencePerHunger != 0) {
            throw fault(id, "round %d does not divide by hunger rate %d"
                    .formatted(essencePerRound, essencePerHunger));
        }
    }

    private static IllegalStateException fault(String id, String what) {
        return new IllegalStateException("Gu spec for '%s' %s".formatted(id, what));
    }
    //endregion
}

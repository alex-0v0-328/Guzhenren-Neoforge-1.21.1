package com.unknown.guzhenren.item;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public sealed interface GuClock {

    void bind(ServerPlayer player, ItemStack stack);

    boolean tick(ServerPlayer player, ItemStack stack, long days);

    boolean hungry(ServerPlayer player, ItemStack stack);

    void warn(ServerPlayer player, ItemStack stack, long days);

    boolean eat(TendedGuItem gu, ServerPlayer player, ItemStack stack, ItemStack food);

    int spareAboveFloor(ItemStack stack);

    int essencePerHungerPoint();

    void pour(ItemStack stack, int from, int to);

    boolean spendOnce(ItemStack stack);

    boolean barVisible(ItemStack stack);
    float barFraction(ItemStack stack);

    //region 饱食条 -- the boars, Human Jun [人力钧力流], Flower Boar and All-Out Effort
    record HungerBar(int max, int unitsPerHunger, int essencePerHunger, int perUse) implements GuClock {

        private static RefinedGuState state(ItemStack s) {return TendedGuItem.state(s);}

        private void setHunger(ItemStack stack, int value) {
            RefinedGuState s = state(stack);
            stack.set(ModDataComponents.REFINED_GU_STATE.get(), s.withHunger(Math.clamp(value, 0, max)));
        }

        private int hunger(ItemStack stack) {return state(stack).hunger();}

        @Override
        public void bind(ServerPlayer player, ItemStack stack) {setHunger(stack, max);}

        @Override
        public boolean tick(ServerPlayer player, ItemStack stack, long days) {
            if (days <= 0L) return false;

            int left = hunger(stack) - (int) Math.min(days, max);
            if (left > 0) {
                setHunger(stack, left);
                return false;
            }
            setHunger(stack, 0);
            return !player.hasInfiniteMaterials();
        }

        @Override
        public boolean hungry(ServerPlayer player, ItemStack stack) {return hunger(stack) <= HUNGRY_THRESHOLD;}

        @Override
        public void warn(ServerPlayer player, ItemStack stack, long days) {
            if (days > 0L) TendedGuItem.announceHungry(player, stack);
        }

        @Override
        public boolean eat(TendedGuItem gu, ServerPlayer player, ItemStack stack, ItemStack food) {
            int units = gu.feedUnits(food);
            if (units <= 0) return false;

            int room = max - hunger(stack);
            if (room <= 0) return false;

            int items = Math.min(food.getCount(), room * unitsPerHunger / units);
            int gained = items * units / unitsPerHunger;
            if (gained <= 0) return false;

            if (!player.hasInfiniteMaterials()) {
                int eaten = (gained * unitsPerHunger + units - 1) / units;
                returnEmptyContainers(player, food, eaten);
                food.shrink(eaten);
            }
            setHunger(stack, hunger(stack) + gained);
            return true;
        }

        private static void returnEmptyContainers(ServerPlayer player, ItemStack food, int eaten) {
            if (!food.getItem().hasCraftingRemainingItem()) return;

            ItemStack empties = new ItemStack(food.getItem().getCraftingRemainingItem(), eaten);
            if (!player.getInventory().add(empties)) player.drop(empties, false);
        }

        @Override
        public int spareAboveFloor(ItemStack stack) {
            return Math.max(0, hunger(stack) - CHANNEL_HUNGER_FLOOR) * essencePerHunger;
        }

        @Override
        public int essencePerHungerPoint() {return essencePerHunger;}

        @Override
        public void pour(ItemStack stack, int from, int to) {
            setHunger(stack, hunger(stack) - (to / essencePerHunger - from / essencePerHunger));
        }

        @Override
        public boolean spendOnce(ItemStack stack) {
            boolean forced = hunger(stack) <= 0;
            setHunger(stack, hunger(stack) - Math.max(1, perUse));
            return forced;
        }

        @Override
        public boolean barVisible(ItemStack stack) {return hunger(stack) < max;}

        @Override
        public float barFraction(ItemStack stack) {return hunger(stack) / (float) max;}
    }
    //endregion

    //region 喂食时钟 -- Liquor Worm [酒虫] and Primeval Elder Gu [元老蛊]
    record FedClock(int mealItems) implements GuClock {

        public static final int WINDOW_TICKS = 2 * Ticks.DAY;
        public static final int WARN_AFTER_TICKS = WINDOW_TICKS + Ticks.HALF_DAY;
        public static final int DEATH_AFTER_TICKS = WINDOW_TICKS + Ticks.DAY;

        public static final int BAR_UNIT_TICKS = 1000;
        public static final int BAR_UNITS = DEATH_AFTER_TICKS / BAR_UNIT_TICKS;

        public static long fedAt(ItemStack stack) {
            return stack.getOrDefault(ModDataComponents.FED_AT.get(), 0L);
        }

        public static long now(ServerPlayer player) {return player.server.overworld().getDayTime();}

        public static long age(ServerPlayer player, ItemStack stack) {
            return Math.max(0L, now(player) - fedAt(stack));
        }

        public static int left(ItemStack stack) {
            return stack.getOrDefault(ModDataComponents.FED_LEFT.get(), BAR_UNITS);
        }

        public boolean needsMeal(ServerPlayer player, ItemStack stack) {
            return age(player, stack) >= WINDOW_TICKS;
        }

        @Override
        public void bind(ServerPlayer player, ItemStack stack) {
            stack.set(ModDataComponents.FED_AT.get(), now(player));
            stack.set(ModDataComponents.FED_LEFT.get(), BAR_UNITS);
        }

        @Override
        public boolean tick(ServerPlayer player, ItemStack stack, long days) {
            if (age(player, stack) >= DEATH_AFTER_TICKS) return true;

            long units = (DEATH_AFTER_TICKS - age(player, stack)) / BAR_UNIT_TICKS;
            int clamped = (int) Math.clamp(units, 0L, BAR_UNITS);
            if (clamped != left(stack)) stack.set(ModDataComponents.FED_LEFT.get(), clamped);
            return false;
        }

        @Override
        public boolean hungry(ServerPlayer player, ItemStack stack) {
            return age(player, stack) >= WARN_AFTER_TICKS;
        }

        @Override
        public void warn(ServerPlayer player, ItemStack stack, long days) {
            long fed = fedAt(stack);
            if (stack.getOrDefault(ModDataComponents.FED_WARNED.get(), Long.MIN_VALUE) == fed) return;

            stack.set(ModDataComponents.FED_WARNED.get(), fed);
            TendedGuItem.announceHungry(player, stack);
        }

        @Override
        public boolean eat(TendedGuItem gu, ServerPlayer player, ItemStack stack, ItemStack food) {
            if (mealItems <= 0 || !needsMeal(player, stack)) return false;
            if (gu.feedUnits(food) <= 0 || food.getCount() < mealItems) return false;

            if (!player.hasInfiniteMaterials()) food.shrink(mealItems);
            bind(player, stack);
            return true;
        }

        @Override
        public int spareAboveFloor(ItemStack stack) {return Integer.MAX_VALUE;}

        @Override
        public int essencePerHungerPoint() {return Integer.MAX_VALUE;}

        @Override
        public void pour(ItemStack stack, int from, int to) {}

        @Override
        public boolean spendOnce(ItemStack stack) {return false;}

        @Override
        public boolean barVisible(ItemStack stack) {return left(stack) < BAR_UNITS;}

        @Override
        public float barFraction(ItemStack stack) {return left(stack) / (float) BAR_UNITS;}
    }
    //endregion

    //region 无时钟 -- the "reusable, needs no feeding" cell, representable and unbuilt
    record NoClock() implements GuClock {
        @Override public void bind(ServerPlayer player, ItemStack stack) {}
        @Override public boolean tick(ServerPlayer player, ItemStack stack, long days) {return false;}
        @Override public boolean hungry(ServerPlayer player, ItemStack stack) {return false;}
        @Override public void warn(ServerPlayer player, ItemStack stack, long days) {}
        @Override public int spareAboveFloor(ItemStack stack) {return Integer.MAX_VALUE;}
        @Override public int essencePerHungerPoint() {return Integer.MAX_VALUE;}
        @Override public void pour(ItemStack stack, int from, int to) {}
        @Override public boolean spendOnce(ItemStack stack) {return false;}
        @Override public boolean barVisible(ItemStack stack) {return false;}
        @Override public float barFraction(ItemStack stack) {return 0.0F;}

        @Override
        public boolean eat(TendedGuItem gu, ServerPlayer player, ItemStack stack, ItemStack food) {return false;}
    }
    //endregion

    int HUNGRY_THRESHOLD = 1;
    int CHANNEL_HUNGER_FLOOR = HUNGRY_THRESHOLD + 1;
}

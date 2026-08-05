package com.unknown.guzhenren.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.registry.ModRecipes;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GuRecipe(List<SizedIngredient> ingredients, List<ItemStack> results, long essencePerSecond,
                       List<Integer> windows, int baseSuccess) implements Recipe<GuRecipeInput> {

    public static final int WINDOW_TICKS = 5 * Ticks.SECOND;
    public static final int GAP_TICKS = 2 * Ticks.SECOND;

    public record Shape(RefinementMode mode, int guInputs) {
        public boolean composite() {return guInputs >= 2;}
    }

    //region the ritual's own clock -- the window list IS the stage count
    public int windowCount() {return windows.size();}
    public int stonesFor(int window) {return windows.get(window);}
    public int totalSeconds() {return totalTicks() / Ticks.SECOND;}
    public long essenceToFinish() {return essencePerSecond * totalSeconds();}

    public int totalTicks() {
        return windowCount() * WINDOW_TICKS + Math.max(0, windowCount() - 1) * GAP_TICKS;
    }
    //endregion

    //region matching -- how much each slot owes, or null when this Gu Recipe [蛊方] does not fit
    // TODO: the claim is greedy; a recipe whose ingredients overlap would need real bipartite matching
    public int @Nullable [] claim(GuRecipeInput input) {
        int size = input.size();
        int[] left = new int[size];
        int[] taken = new int[size];
        for (int i = 0; i < size; i++) left[i] = input.getItem(i).getCount();

        for (SizedIngredient need : ingredients) {
            int want = need.count();
            for (int i = 0; i < size && want > 0; i++) {
                if (left[i] <= 0 || !need.ingredient().test(input.getItem(i))) continue;
                int take = Math.min(want, left[i]);
                want -= take;
                left[i] -= take;
                taken[i] += take;
            }
            if (want > 0) return null;
        }
        for (int n : left) {
            if (n > 0) return null;
        }
        return taken;
    }
    //endregion

    //region 炼法 [refinement mode] -- derived from the ranks, never stored; null means undetermined
    public @Nullable Shape shape() {
        Rank top = null;
        int guInputs = 0;

        for (SizedIngredient need : ingredients) {
            ItemStack[] options = need.ingredient().getItems();
            Rank rank = null;
            int guOptions = 0;
            for (ItemStack option : options) {
                if (!(option.getItem() instanceof MortalGuItem gu)) continue;
                if (rank != null && rank != gu.rank()) return null;
                rank = gu.rank();
                guOptions++;
            }
            if (guOptions == 0) continue;
            if (guOptions != options.length) return null;

            guInputs += need.count();
            if (top == null || rank.ordinal() > top.ordinal()) top = rank;
        }

        Rank out = highestGuRank(results);
        return top == null || out == null ? null : new Shape(RefinementMode.between(top, out), guInputs);
    }

    public int guResultCount() {
        int n = 0;
        for (ItemStack stack : results) {
            if (stack.getItem() instanceof MortalGuItem) n += stack.getCount();
        }
        return n;
    }

    private static @Nullable Rank highestGuRank(List<ItemStack> stacks) {
        Rank top = null;
        for (ItemStack stack : stacks) {
            if (!(stack.getItem() instanceof MortalGuItem gu)) continue;
            if (top == null || gu.rank().ordinal() > top.ordinal()) top = gu.rank();
        }
        return top;
    }
    //endregion

    //region what the recipe manager reads
    @Override
    public boolean matches(@NotNull GuRecipeInput input, @NotNull Level level) {return claim(input) != null;}
    @Override
    public boolean canCraftInDimensions(int width, int height) {return true;}
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {return ModRecipes.REFINEMENT_SERIALIZER.get();}
    @Override
    public @NotNull RecipeType<?> getType() {return ModRecipes.REFINEMENT.get();}

    @Override
    public @NotNull ItemStack assemble(@NotNull GuRecipeInput in, HolderLookup.Provider r) {
        return first().copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {return first();}

    private ItemStack first() {return results.isEmpty() ? ItemStack.EMPTY : results.getFirst();}

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient need : ingredients) list.add(need.ingredient());
        return list;
    }
    //endregion

    public static final class Serializer implements RecipeSerializer<GuRecipe> {

        private static final MapCodec<GuRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                SizedIngredient.FLAT_CODEC.listOf().fieldOf("ingredients").forGetter(GuRecipe::ingredients),
                ItemStack.CODEC.listOf().fieldOf("results").forGetter(GuRecipe::results),
                Codec.LONG.optionalFieldOf("essence_per_second", 0L).forGetter(GuRecipe::essencePerSecond),
                Codec.INT.listOf().optionalFieldOf("windows", List.of()).forGetter(GuRecipe::windows),
                Codec.INT.optionalFieldOf("base_success", 100).forGetter(GuRecipe::baseSuccess)
        ).apply(i, GuRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GuRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), GuRecipe::ingredients,
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), GuRecipe::results,
                ByteBufCodecs.VAR_LONG, GuRecipe::essencePerSecond,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), GuRecipe::windows,
                ByteBufCodecs.VAR_INT, GuRecipe::baseSuccess,
                GuRecipe::new);

        @Override
        public @NotNull MapCodec<GuRecipe> codec() {return CODEC;}
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GuRecipe> streamCodec() {return STREAM_CODEC;}
    }
}

package com.unknown.guzhenren.datagen.recipe;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.MortalGuItem;
import com.unknown.guzhenren.recipe.GuRecipe;
import com.unknown.guzhenren.registry.ModItems;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public class ModRecipeProvider extends RecipeProvider {

    private static final String FOLDER = "refinement/";

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        refinement(output, ModItems.FOUR_FLAVORS_LIQUOR_WORM, 1L, 50, List.of(16, 16, 16, 16),
                SizedIngredient.of(ModItems.LIQUOR_WORM.get(), 2),
                SizedIngredient.of(ModItems.LIQUOR.get(), 1),
                SizedIngredient.of(ModItems.LIQUOR.get(), 1),
                SizedIngredient.of(ModItems.LIQUOR.get(), 1),
                SizedIngredient.of(ModItems.LIQUOR.get(), 1));

        refinement(output, ModItems.ALL_OUT_EFFORT_GU_4, 100L, 30, List.of(64, 64, 64, 64),
                SizedIngredient.of(ModItems.ALL_OUT_EFFORT_GU_3.get(), 1),
                SizedIngredient.of(Items.IRON_BLOCK, 16),
                SizedIngredient.of(Items.GOLD_BLOCK, 8));
    }

    private static void refinement(RecipeOutput output, ItemLike result, long essencePerSecond,
                                   int baseSuccess, List<Integer> windows,
                                   SizedIngredient... ingredients) {
        Item item = result.asItem();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Guzhenren.MOD_ID, FOLDER + BuiltInRegistries.ITEM.getKey(item).getPath());
        output.accept(id, new GuRecipe(List.of(ingredients), List.of(new ItemStack(item)),
                essencePerSecond, windows, baseSuccess), null);
    }
}

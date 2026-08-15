package com.unknown.guzhenren.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * What the refinement [炼蛊] grid is holding, in the shape the recipe manager wants to see it.
 *
 * <p>Implements {@link net.minecraft.world.item.crafting.RecipeInput} as an immutable snapshot of the
 * grid's slots. Built from a {@link net.minecraft.world.Container} via {@code of()}, which copies each
 * stack out so the recipe match never sees a live mutation.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.recipe.GuRecipe
 */
public record GuRecipeInput(List<ItemStack> slots) implements RecipeInput {

    public static GuRecipeInput of(Container container) {
        List<ItemStack> slots = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) slots.add(container.getItem(i));
        return new GuRecipeInput(List.copyOf(slots));
    }

    @Override
    public ItemStack getItem(int index) {return slots.get(index);}
    @Override
    public int size() {return slots.size();}
}

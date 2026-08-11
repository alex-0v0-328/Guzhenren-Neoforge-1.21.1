package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.recipe.GuRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The recipe type and serializer behind refinement [炼蛊].
 *
 * @author Alex
 * @since 1.0.0
 */
public final class ModRecipes {

    private ModRecipes() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Guzhenren.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Guzhenren.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GuRecipe>> REFINEMENT =
            RECIPE_TYPES.register("refinement", id -> RecipeType.<GuRecipe>simple(id));

    public static final DeferredHolder<RecipeSerializer<?>, GuRecipe.Serializer> REFINEMENT_SERIALIZER =
            RECIPE_SERIALIZERS.register("refinement", id -> new GuRecipe.Serializer());

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

package com.unknown.guzhenren.item.material;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;

public class LiquorItem extends GuMaterialItem {

    private static final int NUTRITION = 1;
    private static final float SATURATION = 0.1F;

    private static final float NAUSEA_CHANCE = 0.6F;
    private static final int NAUSEA_TICKS = 300;

    public LiquorItem(Properties properties) {
        super(properties.food(liquor()), Rank.ONE, GuPath.FOOD);
    }

    private static FoodProperties liquor() {
        return new FoodProperties.Builder()
                .nutrition(NUTRITION)
                .saturationModifier(SATURATION)
                .alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS), NAUSEA_CHANCE)
                .build();
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {return UseAnim.DRINK;}
}

package com.unknown.guzhenren.item.material;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.GuMaterialItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;

//  Liquor [酒]: what every Liquor Worm [酒虫] drinks, and a drink in its own right -- three sips in five
//  leave you reeling.
//  ⚠ It earns a class only because it DRINKS rather than eats; the food itself is plain Properties data.
//  A Gu material with no behavior still registers as a bare GuMaterialItem.  CLAUDE.md "Items".
public class LiquorItem extends GuMaterialItem {

    //  Nourishing almost nothing -- this is a drink, not a meal.
    private static final int NUTRITION = 1;
    private static final float SATURATION = 0.1F;

    //  Three sips in five. Fifteen seconds is vanilla's own pufferfish reading of "that was a mistake".
    private static final float NAUSEA_CHANCE = 0.6F;
    private static final int NAUSEA_TICKS = 300;

    public LiquorItem(Properties properties) {
        super(properties.food(liquor()), Rank.ONE, GuPath.FOOD);
    }

    //  ⚠ alwaysEdible: a full stomach is no reason a cultivator cannot drink, and the nausea is the
    //  point of the item -- gating it behind hunger would hide the whole effect from a fed player.
    //  ⚠⚠ The effect goes in as a SUPPLIER, not an instance: NeoForge deprecated the eager overload --
    //  a MobEffectInstance resolves its Holder too early, while the registry is still filling.  CLAUDE.md.
    private static FoodProperties liquor() {
        return new FoodProperties.Builder()
                .nutrition(NUTRITION)
                .saturationModifier(SATURATION)
                .alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS), NAUSEA_CHANCE)
                .build();
    }

    //  ⚠ Drink, not eat. GuItem returns NONE while it owns the click and defers to vanilla otherwise,
    //  and vanilla reads EAT off the food component -- so the one override left to make is this one.
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {return UseAnim.DRINK;}
}

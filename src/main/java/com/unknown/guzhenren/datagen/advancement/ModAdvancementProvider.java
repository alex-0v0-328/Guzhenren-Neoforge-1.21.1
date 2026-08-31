package com.unknown.guzhenren.datagen.advancement;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.advancement.ModCriteriaTriggers;
import com.unknown.guzhenren.registry.item.ModItems;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

/**
 * The advancement tree [进度]: a root per milestone family, generated under {@code advancement/}.
 * The tree opens with the first Hope Gu [希望蛊] ritual -- obtaining it and holding the ritual to the
 * end are two AND-ed criteria ({@code obtained_gu} + {@code used_gu}).
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.advancement.HopeGuUsedTrigger
 * @since 1.0.0
 */

public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                  ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new GuzhenrenAdvancements()));
    }

    private static class GuzhenrenAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.@NotNull Provider registries,
                             @NotNull Consumer<AdvancementHolder> saver,
                             @NotNull ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                .display(new ItemStack(ModItems.HOPE_GU.get()),
                    Component.translatable("advancements.guzhenren.first_awakening.title"),
                    Component.translatable("advancements.guzhenren.first_awakening.description"),
                    ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                    AdvancementType.TASK, true, true, false)
                .addCriterion("obtained_gu", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.HOPE_GU.get()))
                .addCriterion("used_gu", ModCriteriaTriggers.USED_HOPE_GU.get().criterion())
                .requirements(AdvancementRequirements.Strategy.AND)
                .save(saver, Guzhenren.id("first_awakening"), existingFileHelper);
        }
    }
}

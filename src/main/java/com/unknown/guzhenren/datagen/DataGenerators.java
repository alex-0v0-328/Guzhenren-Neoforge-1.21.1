package com.unknown.guzhenren.datagen;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.datagen.curios.ModCuriosProvider;
import com.unknown.guzhenren.datagen.damage.ModDamageTypeTagsProvider;
import com.unknown.guzhenren.datagen.item.ModItemModelProvider;
import com.unknown.guzhenren.datagen.item.ModItemTagsProvider;
import com.unknown.guzhenren.datagen.lang.EnUsLanguageProvider;
import com.unknown.guzhenren.datagen.lang.ZhCnLanguageProvider;
import com.unknown.guzhenren.datagen.recipe.ModRecipeProvider;
import com.unknown.guzhenren.datagen.world.ModBiomeTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class DataGenerators {
    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new EnUsLanguageProvider(packOutput));
        generator.addProvider(event.includeClient(), new ZhCnLanguageProvider(packOutput));

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));

        ModDatapackProvider datapackProvider = generator.addProvider(event.includeServer(),
                new ModDatapackProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(
                packOutput, datapackProvider.getRegistryProvider(), existingFileHelper));

        generator.addProvider(event.includeServer(),
                new ModItemTagsProvider(packOutput, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(),
                new ModCuriosProvider(packOutput, existingFileHelper, lookupProvider));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(),
                new ModBiomeTagsProvider(packOutput, datapackProvider.getRegistryProvider(), existingFileHelper));
    }
}

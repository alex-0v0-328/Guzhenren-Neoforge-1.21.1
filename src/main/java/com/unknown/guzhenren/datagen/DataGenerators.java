package com.unknown.guzhenren.datagen;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.datagen.curios.ModCuriosProvider;
import com.unknown.guzhenren.datagen.damage.ModDamageTypeProvider;
import com.unknown.guzhenren.datagen.damage.ModDamageTypeTagsProvider;
import com.unknown.guzhenren.datagen.item.ModItemModelProvider;
import com.unknown.guzhenren.datagen.item.ModItemTagsProvider;
import com.unknown.guzhenren.datagen.lang.EnUsLanguageProvider;
import com.unknown.guzhenren.datagen.lang.ZhCnLanguageProvider;
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

        ModDamageTypeProvider damageTypeProvider = generator.addProvider(event.includeServer(),
                new ModDamageTypeProvider(packOutput, lookupProvider));

        //  Tag provider must see the types this run generates -- hangs off that provider's registry future.
        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(
                packOutput, damageTypeProvider.getRegistryProvider(), existingFileHelper));

        generator.addProvider(event.includeServer(),
                new ModItemTagsProvider(packOutput, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(),
                new ModCuriosProvider(packOutput, existingFileHelper, lookupProvider));
    }
}

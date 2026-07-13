package com.unknown.guzhenren.datagen;

import com.unknown.guzhenren.Guzhenren;
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

        ModDatapackProvider datapackProvider = generator.addProvider(event.includeServer(),
                new ModDatapackProvider(packOutput, lookupProvider));

        //  The tag provider has to see the damage types this same run is generating, so it hangs off
        //  the datapack provider's registry future rather than the bare vanilla lookup.
        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(
                packOutput, datapackProvider.getRegistryProvider(), existingFileHelper));
    }
}

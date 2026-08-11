package com.unknown.guzhenren.datagen.item;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModItemTags;
import com.unknown.guzhenren.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Writes the item tags, including the larders a Gu feeds from.
 *
 * @author Alex
 * @since 1.0.0
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.<Block>empty()),
                Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModItemTags.BOAR_FEED).add(Items.PORKCHOP);
        tag(ModItemTags.BEAR_FEED).add(Items.HONEY_BOTTLE);
        tag(ModItemTags.BEEF_FEED).add(Items.BEEF);
        tag(ModItemTags.RABBIT_FEED).add(Items.RABBIT);

        tag(ModItemTags.JIN_FEED).add(Items.RAW_IRON);
        tag(ModItemTags.JIN_FEED_DENSE).add(Items.RAW_IRON_BLOCK);
        tag(ModItemTags.JIN_FEED_SMELTED).add(Items.IRON_INGOT);
        tag(ModItemTags.JIN_FEED_SMELTED_DENSE).add(Items.IRON_BLOCK);

        tag(ModItemTags.LIQUOR_FEED).add(ModItems.LIQUOR.get(), ModItems.SOUR_LIQUOR.get(),
                ModItems.SWEET_LIQUOR.get(), ModItems.BITTER_LIQUOR.get(), ModItems.SPICY_LIQUOR.get());

        tag(ModItemTags.ALL_OUT_FEED).add(Items.STONE);

        tag(ModItemTags.ZOMBIE_FEED).add(Items.ROTTEN_FLESH);
    }
}

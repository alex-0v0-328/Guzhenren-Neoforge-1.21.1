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

//  Item tags: what the feedable Gu eat.
//  ⚠ The block-tag future is empty on purpose -- this mod has no blocks for ItemTagsProvider to copy from.
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.<Block>empty()),
                Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModItemTags.BOAR_FEED).add(Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.PIG_SPAWN_EGG);

        //  ⚠ Two tags a tier, not one with both: a block is worth nine, and a tag holds one rate.
        tag(ModItemTags.JIN_FEED).add(Items.RAW_IRON);
        tag(ModItemTags.JIN_FEED_DENSE).add(Items.RAW_IRON_BLOCK);
        tag(ModItemTags.JIN_FEED_SMELTED).add(Items.IRON_INGOT);
        tag(ModItemTags.JIN_FEED_SMELTED_DENSE).add(Items.IRON_BLOCK);

        //  ⚠ Our own liquor only. A sibling mod's drink joins by adding to the tag, not by us guessing
        //  which vanilla item counts -- there is no vanilla liquor, and a potion is not one.
        tag(ModItemTags.LIQUOR_FEED).add(ModItems.LIQUOR.get());
    }
}

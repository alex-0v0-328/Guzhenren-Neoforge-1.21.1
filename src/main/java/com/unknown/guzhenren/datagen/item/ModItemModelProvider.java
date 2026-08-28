package com.unknown.guzhenren.datagen.item;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Writes an item model per registered item, dispatching on the item's class.
 *
 * <p>Extends {@link net.neoforged.neoforge.client.model.generators.ItemModelProvider}. Iterates every
 * registered item and calls {@code basicItem} on it. The texture existence check means a missing PNG
 * fails datagen instead of shipping as a missing-texture item.
 *
 * <p>⚠ It checks that the texture is really there, so a missing PNG fails datagen instead of shipping
 * as a missing-texture item nobody notices until they open the tab.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //    TODO(refactor): exclude custom-model items here when they land (none today).
        for (var entry : ModItems.ITEMS.getEntries()) {
            basicItem(entry.get());
        }
    }
}

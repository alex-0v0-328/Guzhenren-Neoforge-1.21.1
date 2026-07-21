package com.unknown.guzhenren.datagen.item;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

//  Writes assets/guzhenren/models/item/*.json at runData. Not a runtime class --  CLAUDE.md "Conventions".
//  ⚠ basicItem validates the texture, and --existing only points at src/main/resources.
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //  Every registered item gets the flat generated model -- one loop, no per-item line.
        //    TODO(refactor): exclude custom-model items here when they land (none today).
        for (var entry : ModItems.ITEMS.getEntries()) {
            basicItem(entry.get());
        }
    }
}

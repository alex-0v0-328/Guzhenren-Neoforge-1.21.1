package com.unknown.guzhenren.datagen.item;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

//  Writes assets/guzhenren/models/item/*.json at runData. Not a runtime class -- see CLAUDE.md "Conventions".
//  ⚠ basicItem validates the texture, and --existing only points at src/main/resources.
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Guzhenren.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HOPE_GU.get());
        basicItem(ModItems.PRIMEVAL_STONE.get());
    }
}

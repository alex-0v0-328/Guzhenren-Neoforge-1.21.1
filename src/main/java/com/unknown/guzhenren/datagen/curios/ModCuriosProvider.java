package com.unknown.guzhenren.datagen.curios;

import com.unknown.guzhenren.Guzhenren;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

public class ModCuriosProvider extends CuriosDataProvider {

    public ModCuriosProvider(PackOutput output, ExistingFileHelper fileHelper,
                             CompletableFuture<HolderLookup.Provider> registries) {
        super(Guzhenren.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createSlot("hands").size(2);
        createEntities("player").addPlayer().addSlots("hands", "back", "body", "head");
    }
}

package com.unknown.guzhenren.datagen.curios;

import com.unknown.guzhenren.Guzhenren;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

/**
 * Writes the Curios slot data. There is no runtime Curios code in this mod at all.
 *
 * <p>⚠ Curios carries equipment only, and no Gu ever goes into a Curios slot. That decision is why
 * the whole Curios footprint here is datagen.
 *
 * @author Alex
 * @since 1.0.0
 */
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

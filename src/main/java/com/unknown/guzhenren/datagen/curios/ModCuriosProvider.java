package com.unknown.guzhenren.datagen.curios;

import com.unknown.guzhenren.Guzhenren;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

//  Writes data/guzhenren/curios/** at runData. A DataProvider, so it does not exist at runtime --
//  same reason ModDamageTypeProvider lives in datagen/. See CLAUDE.md "Compat".
public class ModCuriosProvider extends CuriosDataProvider {

    public ModCuriosProvider(PackOutput output, ExistingFileHelper fileHelper,
                             CompletableFuture<HolderLookup.Provider> registries) {
        super(Guzhenren.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        //  ⚠ A preset slot type is only an identifier -- Curios puts it on nobody. The entities file is
        //  what attaches it to the player. Only `hands` needs a slot file; preset size is already 1.
        createSlot("hands").size(2);
        createEntities("player").addPlayer().addSlots("hands", "back", "body", "head");
    }
}

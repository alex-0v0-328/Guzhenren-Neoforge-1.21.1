package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.item.GuMaterialItem;
import com.unknown.guzhenren.item.MortalGuItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

//  One tab per branch of the item tree: Mortal Gu [凡蛊] and Gu Material [蛊材].
//  Contents are derived from ModItems by class -- a new item joins its tab with no line here.
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Guzhenren.MOD_ID);

    public static final Supplier<CreativeModeTab> MORTAL_GU = CREATIVE_MODE_TABS.register("mortal_gu",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.guzhenren.mortal_gu"))
                    .icon(() -> new ItemStack(ModItems.HOPE_GU.get()))
                    .displayItems((parameters, output) -> accept(output, MortalGuItem.class))
                    .build());

    public static final Supplier<CreativeModeTab> GU_MATERIAL = CREATIVE_MODE_TABS.register("gu_material",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.guzhenren.gu_material"))
                    .icon(() -> new ItemStack(ModItems.PRIMEVAL_STONE.get()))
                    .displayItems((parameters, output) -> accept(output, GuMaterialItem.class))
                    .build());

    //  Every registered item of this branch, in registration order (DeferredRegister keeps insertion order).
    private static void accept(CreativeModeTab.Output output, Class<? extends Item> branch) {
        for (var entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.get();
            if (branch.isInstance(item)) output.accept(item);
        }
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

//  One tab for the whole mod -- split into 蛊虫 / 蛊材 only once a single tab stops being readable.
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Guzhenren.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.guzhenren.main"))
                    .icon(() -> new ItemStack(ModItems.HOPE_GU.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HOPE_GU.get());
                        output.accept(ModItems.PRIMEVAL_STONE.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

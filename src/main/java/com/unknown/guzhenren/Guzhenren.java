package com.unknown.guzhenren;

import com.mojang.logging.LogUtils;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModCreativeTabs;
import com.unknown.guzhenren.registry.ModDataComponents;
import com.unknown.guzhenren.registry.ModEffects;
import com.unknown.guzhenren.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Guzhenren.MOD_ID)
public class Guzhenren {

    public static final String MOD_ID = "guzhenren";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Guzhenren(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}

package com.unknown.guzhenren.compat.jei;

import com.unknown.guzhenren.Guzhenren;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {return UID;}
}

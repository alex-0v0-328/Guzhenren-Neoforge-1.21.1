package com.unknown.guzhenren.compat.jei;

import com.unknown.guzhenren.Guzhenren;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * An empty JEI plugin: a hook left deliberately unfilled, not an oversight.
 *
 * @author Alex
 * @since 1.0.0
 */
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {return UID;}
}

package com.unknown.guzhenren.compat.jei;

import com.unknown.guzhenren.Guzhenren;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * An empty JEI plugin: a hook left deliberately unfilled, not an oversight.
 *
 * <p>Implements {@link mezz.jei.api.IModPlugin} annotated {@code @JeiPlugin}. JEI is an optional
 * dependency; this class exists so the mod registers its plugin UID and is ready for a future recipe
 * category without adding one now.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            Guzhenren.id("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {return UID;}
}

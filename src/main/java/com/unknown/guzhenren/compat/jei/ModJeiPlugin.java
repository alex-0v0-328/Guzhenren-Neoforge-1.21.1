package com.unknown.guzhenren.compat.jei;

import com.unknown.guzhenren.Guzhenren;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//  JEI entry point. Empty on purpose: every IModPlugin hook defaults, and the mod's 12 items have no
//  recipes yet -- this is the hook a recipe category will land on.
//  Mod*, not a bare JeiPlugin -- that name is taken by the annotation. Loaded only when JEI is present.
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {return UID;}
}

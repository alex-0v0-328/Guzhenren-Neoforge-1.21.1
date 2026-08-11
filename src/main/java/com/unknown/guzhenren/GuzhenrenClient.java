package com.unknown.guzhenren;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Client-only entry point, so that nothing which would crash a dedicated server sits in {@link Guzhenren}.
 *
 * @author Alex
 * @since 1.0.0
 */
@Mod(value = Guzhenren.MOD_ID, dist = Dist.CLIENT)
public class GuzhenrenClient {

    public GuzhenrenClient(ModContainer container) {}
}

package com.unknown.guzhenren;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Client-only entry point, so that nothing which would crash a dedicated server sits in {@link Guzhenren}.
 *
 * <p>Annotated {@code @Mod(dist = Dist.CLIENT)}; the constructor is empty today, but its existence is
 * what keeps client registrations out of the common mod class. All client-side event subscribers live
 * under the {@code client/} package tree and are loaded only on the client.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see Guzhenren
 */
@Mod(value = Guzhenren.MOD_ID, dist = Dist.CLIENT)
public class GuzhenrenClient {

    public GuzhenrenClient(ModContainer container) {}
}

package com.unknown.guzhenren;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

//  The client entrypoint. Deliberately bare: every client hook lives in client/ as an @EventBusSubscriber,
//  and the mod has no config, so the template's ConfigurationScreen opened an empty one.
@Mod(value = Guzhenren.MOD_ID, dist = Dist.CLIENT)
public class GuzhenrenClient {

    public GuzhenrenClient(ModContainer container) {}
}

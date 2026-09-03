package com.unknown.guzhenren.event.compat;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

/**
 * Sets Epic Fight's per-level skill-retention rule for every loaded server level.
 */

@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class EpicFightServerEvents {

    private EpicFightServerEvents() {}
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            EpicFightGameRules.KEEP_SKILLS.setRuleValue(level, true);
        }
    }
}

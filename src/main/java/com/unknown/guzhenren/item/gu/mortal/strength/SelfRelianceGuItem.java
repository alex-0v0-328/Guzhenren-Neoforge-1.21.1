package com.unknown.guzhenren.item.gu.mortal.strength;

import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.item.gu.mortal.BuffGuItem;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Self-Reliance Gu [自力更生蛊] item: a tended buff Gu that drives ITSELF at the brink.
 *
 * <p>{@code tryAutoUse} is the heartbeat's call -- when the holder is under 20% health and not
 * already running the effect, the first copy in the inventory fires on its own.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.effect.timed.SelfRelianceGuEffect
 * @since 1.0.0
 */

public final class SelfRelianceGuItem extends BuffGuItem {

    public SelfRelianceGuItem(Properties properties, int durationTicks, int amplifier, GuSpec spec) {
        super(properties, ModEffects.SELF_RELIANCE_GU, durationTicks, amplifier, spec);
    }

    public static void tryAutoUse(ServerPlayer player) {
        if (player.getHealth() >= player.getMaxHealth() * 0.2F
                || player.hasEffect(ModEffects.SELF_RELIANCE_GU)) return;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SelfRelianceGuItem item && item.autoUse(player, stack)) return;
        }
    }
}

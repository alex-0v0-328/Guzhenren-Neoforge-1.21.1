package com.unknown.guzhenren.item.gu.mortal.time;

import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Watch Gu [更蛊]: for a while its holder's own clock [自身时间] outruns the world's, and then it is gone.
 *
 * <p>⚠ How much faster is read off the amplifier, which is this Gu's tier, so a registration passes only
 * the length. The ladder itself lives on the effect, where every reader of it already looks.
 *
 * @author Alex
 * @since 1.0.0
 */
public class WatchGuItem extends ConsumedGuItem {

    private static final String FAILED_ALREADY_HASTENED = "guzhenren.item.failed.time_flow_active";

    private final int effectTicks;

    public WatchGuItem(Properties properties, int effectTicks, GuSpec spec) {
        super(properties, spec);
        this.effectTicks = effectTicks;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {
        return player.hasEffect(ModEffects.TIME_FLOW) ? new Refusal(FAILED_ALREADY_HASTENED) : null;
    }

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.TIME_FLOW, effectTicks, tier()));
    }
}

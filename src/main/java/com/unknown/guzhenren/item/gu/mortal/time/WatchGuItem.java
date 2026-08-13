package com.unknown.guzhenren.item.gu.mortal.time;

import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Watch Gu [更蛊]: for a while its holder's own clock [自身时间] outruns the world's, and then it is gone.
 *
 * <p>⚠ It refuses nothing. Wearing two Watch Gu at once is the design, and their rates and their specks
 * both add -- the speed and the price it books live on the effect, not here.
 *
 * @author Alex
 * @since 1.0.0
 */
public class WatchGuItem extends ConsumedGuItem {

    private final Holder<MobEffect> form;
    private final int effectTicks;

    public WatchGuItem(Properties properties, Holder<MobEffect> form, int effectTicks, GuSpec spec) {
        super(properties, spec);
        this.form = form;
        this.effectTicks = effectTicks;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {return null;}

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(form, effectTicks));
    }
}

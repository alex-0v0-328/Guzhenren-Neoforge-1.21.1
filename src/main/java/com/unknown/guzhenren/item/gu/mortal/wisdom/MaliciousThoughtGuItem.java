package com.unknown.guzhenren.item.gu.mortal.wisdom;

import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag;
import com.unknown.guzhenren.effect.timed.MaliciousThoughtEffect;
import com.unknown.guzhenren.item.gu.ConsumedGuItem;
import com.unknown.guzhenren.item.gu.GuSpec;
import com.unknown.guzhenren.registry.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The Malicious Thought Gu [恶念蛊]: a one-use wisdom Gu that floods the mind with evil thoughts.
 *
 * <p>Extends {@link com.unknown.guzhenren.item.gu.ConsumedGuItem}, making it tended AND taken by its own
 * use. The payout grants an immediate {@link com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag#EVIL} burst
 * via {@link com.unknown.guzhenren.attachment.service.mind.MindService#addThoughts}, then stamps the
 * sustained effect whose amplifier is derived from the rank.
 *
 * <p>⚠ Rank five floods past the burst line from an empty mind, so it kills on the spot. That is the
 * design, not a bug -- the antidote is thought conversion, unbuilt.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.item.gu.ConsumedGuItem
 * @since 1.0.0
 */

public class MaliciousThoughtGuItem extends ConsumedGuItem {

    private final Holder<MobEffect> effect;
    private final long immediateThoughts;
    public MaliciousThoughtGuItem(Properties properties, Holder<MobEffect> effect, long immediateThoughts,
                                  GuSpec spec) {
        super(properties, spec);
        this.effect = effect;
        this.immediateThoughts = immediateThoughts;
    }
    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {return null;}
    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        MindService.addThoughts(player, immediateThoughts, ThoughtTag.EVIL);
        int amplifier = tier() - 1;
        player.addEffect(ModEffects.instance(effect, MaliciousThoughtEffect.DURATION_TICKS, amplifier));
    }
}

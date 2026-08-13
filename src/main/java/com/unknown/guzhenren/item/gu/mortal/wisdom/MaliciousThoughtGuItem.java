package com.unknown.guzhenren.item.gu.mortal.wisdom;

import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.wisdom.ThoughtTag;
import com.unknown.guzhenren.effect.timed.MaliciousThoughtEffect;
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
 * The Malicious Thought Gu [恶念蛊]: a one-use wisdom Gu that floods the mind with evil thoughts.
 *
 * <p>⚠ Rank five floods past the burst line from an empty mind, so it kills on the spot. That is the
 * design, not a bug -- the antidote is thought conversion, unbuilt.
 *
 * @author Alex
 * @since 1.0.0
 */
public class MaliciousThoughtGuItem extends ConsumedGuItem {

    private final Holder<MobEffect> effect;
    private final long immediateThoughts;

    public MaliciousThoughtGuItem(Properties properties, Holder<MobEffect> effect, long immediateThoughts, GuSpec spec) {
        super(properties, spec);
        this.effect = effect;
        this.immediateThoughts = immediateThoughts;
    }

    @Override
    protected @Nullable Refusal payoutGate(Player player, ItemStack stack) {return null;}

    @Override
    protected void payout(ServerPlayer player, ItemStack stack) {
        MindService.addThoughts(player, immediateThoughts, ThoughtTag.EVIL);
        int amplifier = spec.rank().ordinal() - Rank.TWO.ordinal();
        player.addEffect(new MobEffectInstance(effect, MaliciousThoughtEffect.DURATION_TICKS, amplifier));
    }
}

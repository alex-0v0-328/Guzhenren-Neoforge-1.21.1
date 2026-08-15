package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Max health as a transient {@link AttributeModifier} derived from rank.
 *
 * <p>Static service; fires from {@link ApertureService#store} on every aperture write, plus login,
 * clone and reset (a modifier does not ride a clone). The modifier is keyed to the rank's
 * {@code getMaxHealth()} minus vanilla's 20, so a mortal ({@code NONE}) reads bonus 0 and the service
 * no-ops.
 *
 * <p>⚠ The modifier MUST stay transient. A permanent one is saved into attribute NBT and then fights
 * the next login, stacking itself on top of what was already stored there. ⚠ {@code refresh} is a
 * no-op when the bonus has not moved -- it is called from every aperture write, so skipping the no-op
 * check would re-issue the modifier twice a second forever. ⚠ Lowering the cap must also clamp
 * current health down (the last line) -- {@link AttackService} does not need that, because attack has
 * no "current" to overflow.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see AttackService
 * @see ApertureService
 */
public final class HealthService {

    private HealthService() {}

    public static final double VANILLA_MAX_HEALTH = 20.0D;

    private static final ResourceLocation MODIFIER_ID =
            Guzhenren.id("rank_max_health");

    public static void refresh(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;

        int target = ApertureService.rank(player).getMaxHealth();
        double bonus = target > 0 ? target - VANILLA_MAX_HEALTH : 0.0D;

        AttributeModifier held = instance.getModifier(MODIFIER_ID);
        if (held == null ? bonus == 0.0D : held.amount() == bonus) return;

        instance.removeModifier(MODIFIER_ID);
        if (bonus != 0.0D) {
            instance.addTransientModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
}

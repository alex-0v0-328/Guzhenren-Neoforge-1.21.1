package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Max health as an AttributeModifier derived from rank, which is why it does not ride a clone.
 *
 * <p>⚠ Transient, and a no-op when the rank has not moved. Lowering the cap must also clamp current
 * health down -- the one line {@link AttackService} does not need.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class HealthService {

    private HealthService() {}

    public static final double VANILLA_MAX_HEALTH = 20.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "rank_max_health");

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

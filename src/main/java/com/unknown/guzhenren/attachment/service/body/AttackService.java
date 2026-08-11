package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.effect.AttackContributor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * The only thing in the mod that touches ATTACK_DAMAGE -- {@code bonus()} is the whole sum.
 *
 * <p>⚠ The modifier must stay transient. A permanent one is saved into attribute NBT and then fights
 * the next login, stacking itself on top of what was already stored there.
 *
 * @author Alex
 * @since 1.0.0
 * @see HealthService
 */
public final class AttackService {

    private AttackService() {}

    public static final double VANILLA_ATTACK_DAMAGE = 1.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "strength_attack_damage");

    public static final double ZOMBIE_ATTACK_BASE = 5.0D;

    public static double bonus(Player player) {
        StrengthData data = StrengthService.get(player);
        double total = 0.0D;

        for (BeastStrength beast : BeastStrength.values()) {
            if (data.has(beast)) total += beast.getAttackBonus();
        }
        return total + StrengthService.usableJin(player) * HumanStrength.ATTACK_PER_JIN
                + zombieBonus(player) + effectBonus(player);
    }

    public static double effectBonus(Player player) {
        double total = 0.0D;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof AttackContributor contributor) {
                total += contributor.attackBonus(instance.getAmplifier());
            }
        }
        return total;
    }

    public static double zombieBonus(Player player) {
        BodyData body = BodyService.get(player);
        if (!body.lifeForm().isAnyZombie() || body.zombieTier() < 0) return 0.0D;

        return ZOMBIE_ATTACK_BASE * (1 << body.zombieTier());
    }

    public static void refresh(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;

        double bonus = bonus(player);
        AttributeModifier held = instance.getModifier(MODIFIER_ID);
        if (held == null ? bonus == 0.0D : held.amount() == bonus) return;

        instance.removeModifier(MODIFIER_ID);
        if (bonus != 0.0D) {
            instance.addTransientModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}

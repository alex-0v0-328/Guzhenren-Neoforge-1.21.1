package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class AttackService {

    private AttackService() {}

    public static final double VANILLA_ATTACK_DAMAGE = 1.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "strength_attack_damage");

    public static double bonus(Player player) {
        StrengthData data = StrengthService.get(player);
        double total = 0.0D;

        for (BeastStrength beast : BeastStrength.values()) {
            if (data.has(beast)) total += beast.getAttackBonus();
        }
        for (HumanStrength kind : HumanStrength.values()) {
            int steps = data.humanStrengthCount(kind) / kind.getLayersPerStep();
            total += steps * kind.getAttackBonus();
        }
        return total;
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

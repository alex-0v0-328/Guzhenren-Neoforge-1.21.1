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

//  Attack damage follows the Strength Path [力道]: a beast strength taken, or a Human Jun [人力钧力流]
//  kind trained to its ceiling. ⚠ An attribute modifier is NOT an attachment -- it does not ride a clone,
//  so every entry point re-applies. The sibling of HealthService, and it copies its two traps.
public final class AttackService {

    private AttackService() {}

    //  Vanilla's own player base. The modifier carries only what 力道 adds on top, so nothing else
    //  needs to know it -- a White Boar [白豕蛊] alone therefore reads as 3.0 in game.
    public static final double VANILLA_ATTACK_DAMAGE = 1.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "strength_attack_damage");

    //  Reads take Player, so the value can be shown client-side without a second formula.
    public static double bonus(Player player) {
        StrengthData data = StrengthService.get(player);
        double total = 0.0D;

        //  Taken once ever, so the set membership IS the condition.
        for (BeastStrength beast : BeastStrength.values()) {
            if (data.has(beast)) total += beast.getAttackBonus();
        }
        //  ⚠⚠ A STEP function, not a rate: whole steps only, so two layers of 斤力 are worth ZERO and the
        //  remainder buys nothing until the next step completes. Integer division IS the mechanic.
        for (HumanStrength kind : HumanStrength.values()) {
            int steps = data.humanStrengthCount(kind) / kind.getLayersPerStep();
            total += steps * kind.getAttackBonus();
        }
        return total;
    }

    //  ⚠ Transient, not permanent: a permanent modifier is written into the player's attribute NBT, and
    //  that saved copy would come back on the next login and fight the one this writes.
    public static void refresh(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;

        double bonus = bonus(player);
        //  ⚠ Must cost nothing when nothing moved -- re-adding syncs the attribute to the client, and
        //  every StrengthData write lands here. Same guard HealthService needs for essence regen.
        AttributeModifier held = instance.getModifier(MODIFIER_ID);
        if (held == null ? bonus == 0.0D : held.amount() == bonus) return;

        instance.removeModifier(MODIFIER_ID);
        if (bonus != 0.0D) {
            instance.addTransientModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}

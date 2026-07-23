package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

//  Max health follows the rank: Rank I 20 .. Rank V 100, stages not involved. See Rank.maxHealth.
//  ⚠ An attribute modifier is NOT an attachment -- it does not ride a clone, so every entry point re-applies.
public final class HealthService {

    private HealthService() {}

    //  Vanilla's own base. The modifier carries only the difference, so nothing else needs to know it.
    public static final double VANILLA_MAX_HEALTH = 20.0D;

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Guzhenren.MOD_ID, "rank_max_health");

    //  ⚠ Transient, not permanent: a permanent modifier is written into the player's attribute NBT, and
    //  that saved copy would come back on the next login and fight the one this writes.
    public static void refresh(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;

        //  ⚠ Rank.SIX..NINE carry 0 the way rankBase does -- "no immortal system yet", not "no health".
        int target = ApertureService.rank(player).getMaxHealth();
        double bonus = target > 0 ? target - VANILLA_MAX_HEALTH : 0.0D;

        //  ⚠ Every aperture write lands here -- essence regen too, once a second. So it must cost nothing
        //  when the rank has not moved, or re-adding syncs the attribute to the client every heartbeat.
        AttributeModifier held = instance.getModifier(MODIFIER_ID);
        if (held == null ? bonus == 0.0D : held.amount() == bonus) return;

        instance.removeModifier(MODIFIER_ID);
        if (bonus != 0.0D) {
            instance.addTransientModifier(
                    new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }

        //  Dropping the cap leaves current health above it until something pushes it down.
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
}

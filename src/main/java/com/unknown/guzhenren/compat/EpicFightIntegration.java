package com.unknown.guzhenren.compat;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.entity.HopeGuEntity;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.ComboAttackEvent;
import yesman.epicfight.api.event.types.player.SetTargetEvent;
import yesman.epicfight.api.event.types.player.SkillConsumeEvent;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/**
 * The one required Epic Fight bridge: aptitude cap, undead free skill use, attack refresh, and Hope Gu
 * target exclusion.
 *
 * <p>The old GZR stamina attachment, sprint gate, jump bill, hunger exhaustion surcharge and client mixin are
 * deliberately absent. Epic Fight owns current stamina, regeneration, HUD and all ordinary consumption.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */
public final class EpicFightIntegration {

    private static final ResourceLocation STAMINA_MODIFIER = Guzhenren.id("epic_fight_stamina");

    private EpicFightIntegration() {}

    public static void initialize() {
        EpicFightEventHooks.Player.CONSUME_SKILL.registerEvent(EpicFightIntegration::onSkillConsume, Guzhenren.MOD_ID);
        EpicFightEventHooks.Player.COMBO_ATTACK.registerEvent(EpicFightIntegration::onComboAttack, Guzhenren.MOD_ID);
        EpicFightEventHooks.Player.SET_TARGET.registerEvent(EpicFightIntegration::onSetTarget, Guzhenren.MOD_ID);
    }

    public static void refresh(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(EpicFightAttributes.MAX_STAMINA);
        if (instance == null) return;

        double bonus = staminaMaxPercent(player) / 100.0D;
        AttributeModifier held = instance.getModifier(STAMINA_MODIFIER);
        if (held != null && held.amount() == bonus
                && held.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) return;

        instance.removeModifier(STAMINA_MODIFIER);
        if (bonus != 0.0D) {
            instance.addTransientModifier(new AttributeModifier(STAMINA_MODIFIER, bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        EpicFightCapabilities.getServerPlayerPatch(player).clampMaxAttributes();
    }

    private static int staminaMaxPercent(ServerPlayer player) {
        var aperture = ApertureService.aperture(player);
        return aperture.extremePhysique() == ExtremePhysique.NONE
                ? aperture.talent().getStaminaMaxPercent()
                : aperture.extremePhysique().getStaminaMaxPercent();
    }

    private static void onSkillConsume(SkillConsumeEvent event) {
        if (event.getResourceType() != Skill.Resource.STAMINA) return;
        if (event.getEntityPatch().getOriginal() instanceof Player player
                && !BodyService.lifeForm(player).spendsStamina()) {
            event.setAmount(0.0F);
        }
    }

    private static void onComboAttack(ComboAttackEvent event) {
        ServerPlayer player = event.getPlayerPatch().getOriginal();
        if (player.hasEffect(ModEffects.HARDSHIP_STRENGTH_GU)) AttackService.refresh(player);
    }

    private static void onSetTarget(SetTargetEvent event) {
        if (event.getTarget() instanceof HopeGuEntity) event.cancel();
    }
}

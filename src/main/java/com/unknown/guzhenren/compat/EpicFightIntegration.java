package com.unknown.guzhenren.compat;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyAttackService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.entity.WildGuEntity;
import com.unknown.guzhenren.registry.effect.ModEffects;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.ComboAttackEvent;
import yesman.epicfight.api.event.types.player.SetTargetEvent;
import yesman.epicfight.api.event.types.player.SkillConsumeEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * The one required Epic Fight bridge: aptitude cap, undead free skill use, attack refresh, and target
 * exclusion for every {@link com.unknown.guzhenren.entity.WildGuEntity}.
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
    private static final double DASH_COORD_SCALE = 3.0D;

    private static AnimationManager.AnimationAccessor<DodgeAnimation> DASH_FORWARD;
    private static AnimationManager.AnimationAccessor<DodgeAnimation> DASH_BACKWARD;

    private EpicFightIntegration() {}

    public static void initialize() {
        EpicFightEventHooks.Player.CONSUME_SKILL.registerEvent(EpicFightIntegration::onSkillConsume, Guzhenren.MOD_ID);
        EpicFightEventHooks.Player.COMBO_ATTACK.registerEvent(EpicFightIntegration::onComboAttack, Guzhenren.MOD_ID);
        EpicFightEventHooks.Player.SET_TARGET.registerEvent(EpicFightIntegration::onSetTarget, Guzhenren.MOD_ID);
    }

    public static void onAnimationRegistry(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(Guzhenren.MOD_ID, builder -> {
            DASH_FORWARD = builder.nextAccessor("biped/skill/dash_forward", accessor ->
                    new DashAnimation(0.1F, accessor, 0.8F, 0.6F, Armatures.BIPED)
                            .setResourceLocation("epicfight", "biped/skill/step_forward"));
            DASH_BACKWARD = builder.nextAccessor("biped/skill/dash_backward", accessor ->
                    new DashAnimation(0.1F, accessor, 0.8F, 0.6F, Armatures.BIPED)
                            .setResourceLocation("epicfight", "biped/skill/step_backward"));
        });
    }

    public static void dash(ServerPlayer player, int vertical, float yRot) {
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null) return;
        if (!EpicFightSkills.STEP.get().isExecutableState(patch)) return;

        AnimationManager.AnimationAccessor<DodgeAnimation> animation = vertical < 0
                ? DASH_BACKWARD : DASH_FORWARD;
        if (animation == null) return;
        patch.playAnimationSynchronized(animation, 0.0F);
        patch.setModelYRot(yRot, true);
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
        Objects.requireNonNull(EpicFightCapabilities.getServerPlayerPatch(player)).clampMaxAttributes();
    }

    private static int staminaMaxPercent(ServerPlayer player) {
        ExtremePhysique physique = BodyService.extremePhysique(player);
        return physique == ExtremePhysique.NONE
                ? ApertureService.talent(player).getStaminaMaxPercent()
                : physique.getStaminaMaxPercent();
    }

    private static void onSkillConsume(SkillConsumeEvent event) {
        if (event.getResourceType() != Skill.Resource.STAMINA) return;
        if (event.getEntityPatch().getOriginal() instanceof Player player
                && BodyService.isUndead(player)) {
            event.setAmount(0.0F);
        }
    }

    private static void onComboAttack(ComboAttackEvent event) {
        ServerPlayer player = event.getPlayerPatch().getOriginal();
        if (player.hasEffect(ModEffects.HARDSHIP_STRENGTH_GU)) BodyAttackService.refresh(player);
    }

    private static void onSetTarget(SetTargetEvent event) {
        if (event.getTarget() instanceof WildGuEntity) event.cancel();
    }

    private static final class DashAnimation extends DodgeAnimation {

        private DashAnimation(float transitionTime, AnimationManager.AnimationAccessor<DodgeAnimation> accessor,
                              float width, float height, AssetAccessor<? extends Armature> armature) {
            super(transitionTime, accessor, width, height, armature);
        }

        @Override
        protected Vec3 getCoordVector(LivingEntityPatch<?> entityPatch,
                                      AssetAccessor<? extends DynamicAnimation> animation) {
            return super.getCoordVector(entityPatch, animation).scale(DASH_COORD_SCALE);
        }
    }
}

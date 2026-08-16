package com.unknown.guzhenren.event;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.service.aperture.ApertureStorageTick;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.aperture.NourishService;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.TimeFlowService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.effect.pool.DeathQiEffect;
import com.unknown.guzhenren.item.gu.TendedGuItem;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import com.unknown.guzhenren.registry.ModDamageTypes;
import com.unknown.guzhenren.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The one-second heartbeat: a straight run of fourteen steps that most of the player's state depends on.
 *
 * <p>Every step runs inside {@code tickCount % Ticks.SECOND == 0}, and most read what an earlier one
 * just wrote — aging feeds the day-clock walks, {@code syncEffects} feeds {@code tickDeathQi} and
 * {@code regenStep}, {@code tickHalfZombie} feeds attack and regen. {@link
 * com.unknown.guzhenren.attachment.PlayerDataService} owns the lifecycle; this file owns the cadence.
 *
 * <p>⚠ The step ORDER is load-bearing and nothing in the code admits it. Reorder nothing blind;
 * a new step must declare which existing one it follows and why. {@code checkLethalState} runs last
 * and returns after the first hit, so the three deaths have a fixed precedence: 寿元 → 魂魄 → 脑海.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.attachment.service.body.BodyService
 */
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class PlayerTickEvents {

    private PlayerTickEvents() {}

    private static final int FULL_HUNGER = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isRemoved() || player.isDeadOrDying()) return;

        if (player.tickCount % EssenceService.REGEN_INTERVAL_TICKS != 0) return;

        long days = BodyService.tickAging(player);
        TendedGuItem.tickCarried(player, days);
        ApertureStorageTick.tickStored(player, days);

        if (days > 0L && player.containerMenu instanceof ApertureStorageMenu menu) menu.reload();

        closeDistilling(player);
        tickHalfZombie(player);
        pinUndeadHunger(player);
        QiService.syncEffects(player);
        tickDeathQi(player);
        AttackService.refresh(player);
        TimeFlowService.syncSpecks(player);
        BodyService.tickLifespan(player);
        EssenceService.regenStep(player);
        NourishService.tickNourish(player);
        MindService.regenStep(player);
        checkLethalState(player);
    }

    private static void tickDeathQi(ServerPlayer player) {
        if (!player.hasEffect(ModEffects.DEATH_QI) || BodyService.isZombie(player)) return;

        if (player.tickCount % DeathQiEffect.YEAR_INTERVAL_TICKS == 0) {
            BodyService.drainByDeathQi(player, DeathQiEffect.YEARS_PER_INTERVAL);
        }
        if (player.getHealth() > DeathQiEffect.HEALTH_FLOOR) {
            player.setHealth(Math.max(DeathQiEffect.HEALTH_FLOOR,
                    player.getHealth() - DeathQiEffect.HEALTH_PER_HEARTBEAT));
        }
    }

    private static void pinUndeadHunger(ServerPlayer player) {
        if (BodyService.lifeForm(player).getsHungry()) return;

        FoodData food = player.getFoodData();
        food.setFoodLevel(FULL_HUNGER);
        food.setSaturation(FULL_HUNGER);
        food.setExhaustion(0.0F);
    }

    private static void tickHalfZombie(ServerPlayer player) {
        if (BodyService.isHalfZombie(player)) {
            if (QiService.current(player, QiKind.DEATH) > 0L) {
                BodyService.setLifeForm(player, LifeForm.ZOMBIE);
            } else if (BodyService.halfZombieRanOut(player)) {
                BodyService.setLifeForm(player, LifeForm.ALIVE);
            }
        }
        projectHalfZombie(player);
    }

    private static void projectHalfZombie(ServerPlayer player) {
        if (!BodyService.isHalfZombie(player)) {
            if (player.hasEffect(ModEffects.HALF_ZOMBIE)) player.removeEffect(ModEffects.HALF_ZOMBIE);
            return;
        }
        player.addEffect(new MobEffectInstance(
                ModEffects.HALF_ZOMBIE, (int) BodyService.halfZombieTicksLeft(player), 0, false, false));
    }

    private static void closeDistilling(ServerPlayer player) {
        if (EssenceService.distilledEssence(player) > 0L && !EssenceService.isDistilling(player)) {
            EssenceService.endDistilling(player);
        }
    }

    private static void checkLethalState(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (BodyService.get(player).isExhausted()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.LIFESPAN_EXHAUSTED), Float.MAX_VALUE);
            return;
        }
        if (SoulService.get(player).isCollapsed()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.SOUL_COLLAPSE), Float.MAX_VALUE);
            return;
        }
        if (MindService.get(player).isOverflowing()) {
            player.hurt(ModDamageTypes.source(player, ModDamageTypes.MIND_OCEAN_SHATTERED), Float.MAX_VALUE);
        }
    }
}

package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.StaminaData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Stamina [耐力]: the derived cap, the two sprint gates, and the single door every write passes.
 *
 * <p>Static service over the {@code stamina_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. The cap is derived ({@code baseMax + bonus}), so this service is the only
 * thing that can clamp. {@code step} is the heartbeat entry: sprinting and regen are EXCLUSIVE (one or
 * the other per step), because both at once would net a free sprint for a mortal.
 *
 * <p>⚠ Sprinting needs TWO thresholds, not one: {@code WEARY_PERCENT} 20 (below which the vanilla
 * hunger bar drains 1.5×) sits BELOW {@code SPRINT_RESUME_PERCENT} 30, and that ordering IS the design
 * -- the last fifth of a sprint is spent weary, and a rest crosses back out before you may run again.
 * Invert them and the weary zone becomes unreachable by sprinting. ⚠ {@code store} no-ops when the
 * record has not moved -- otherwise a full pool pushes a sync packet twice a second forever. ⚠ A jump
 * cannot be refused ({@code LivingJumpEvent} is not cancellable); the cost simply floors at 0. ⚠
 * Creative/spectator refill instead of paying, in both {@code step} and {@code spendOnJump}.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see StaminaData
 * @see com.unknown.guzhenren.mixin.LocalPlayerSprintMixin
 */
public final class StaminaService {

    private StaminaService() {}

    public static final long SPRINT_COST_PER_STEP = 1L;
    public static final long JUMP_COST = 3L;

    public static final long SPRINT_RESUME_PERCENT = 30L;
    public static final long WEARY_PERCENT = 20L;

    public static boolean canKeepSprinting(Player p) {return !isEmpty(p);}
    public static boolean canResumeSprinting(Player p) {
        return current(p) * 100L >= max(p) * SPRINT_RESUME_PERCENT;
    }
    public static boolean isWeary(Player p) {return current(p) * 100L < max(p) * WEARY_PERCENT;}

    public static StaminaData get(Player p) {return p.getData(ModAttachments.STAMINA);}
    public static long current(Player p) {return get(p).currentStamina();}
    public static long bonus(Player p) {return get(p).bonusStamina();}
    public static boolean isEmpty(Player p) {return get(p).isEmpty();}
    public static long max(Player p) {return baseMax(p) + bonus(p);}

    public static long baseMax(Player player) {
        Aperture aperture = ApertureService.aperture(player);
        return aperture.extremePhysique() == ExtremePhysique.NONE
                ? aperture.talent().getStaminaBase()
                : aperture.extremePhysique().getStaminaBase();
    }

    public static long regenPerStep(Player player) {
        Aperture aperture = ApertureService.aperture(player);
        return aperture.extremePhysique() == ExtremePhysique.NONE
                ? aperture.talent().getStaminaRegen()
                : aperture.extremePhysique().getStaminaRegen();
    }

    public static void addCurrent(ServerPlayer p, long delta) {setCurrent(p, current(p) + delta);}
    public static void addBonus(ServerPlayer p, long delta) {setBonus(p, bonus(p) + delta);}
    public static void refill(ServerPlayer p) {setCurrent(p, max(p));}
    public static void refresh(ServerPlayer p) {setCurrent(p, current(p));}

    public static void setCurrent(ServerPlayer player, long value) {
        store(player, get(player).withCurrentStamina(Math.clamp(value, 0L, max(player))));
    }

    public static void setBonus(ServerPlayer player, long value) {
        store(player, get(player).withBonusStamina(value));
        refresh(player);
    }

    private static void store(ServerPlayer player, StaminaData data) {
        if (data.equals(get(player))) return;
        player.setData(ModAttachments.STAMINA, data);
    }

    public static boolean consume(ServerPlayer player, long amount) {
        if (amount <= 0L || !BodyService.lifeForm(player).spendsStamina()) return true;
        long current = current(player);
        if (current < amount) return false;
        setCurrent(player, current - amount);
        return true;
    }

    public static void step(ServerPlayer player) {
        if (player.hasInfiniteMaterials() || player.isSpectator()) {
            refill(player);
            return;
        }
        if (player.isSprinting()) {
            drain(player, SPRINT_COST_PER_STEP);
            return;
        }
        addCurrent(player, TimeFlowService.perStep(player, regenPerStep(player)));
    }

    public static void spendOnJump(ServerPlayer player) {
        if (player.hasInfiniteMaterials() || player.isSpectator()) return;
        drain(player, JUMP_COST);
    }

    private static void drain(ServerPlayer player, long amount) {
        if (!BodyService.lifeForm(player).spendsStamina()) return;

        addCurrent(player, -amount);
        if (isEmpty(player)) player.setSprinting(false);
    }
}

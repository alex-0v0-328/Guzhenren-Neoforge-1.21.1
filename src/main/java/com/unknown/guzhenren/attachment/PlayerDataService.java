package com.unknown.guzhenren.attachment;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.attachment.data.aperture.NourishData;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.QiData;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.body.StaminaData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.StaminaService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The one cross-domain lifecycle service: birth, sleep, death, clone, respawn, and a full reset.
 *
 * <p>It is the single place that decides what a clone inherits, because a death-copy and a
 * keepInventory-off {@code resetAll} cannot both be the last write. Every domain service is called
 * from here for the refresh that does not ride a clone -- {@link HealthService}, {@link AttackService}
 * and {@link StaminaService} all re-run on join, clone and reset.
 *
 * <p>⚠ The {@code Player} (not {@code ServerPlayer}) signature on {@code copy}/{@code onBirth}/
 * {@code resetAll} is the one carve-out from this project's read-{@code Player}/write-{@code ServerPlayer}
 * rule: during {@code PlayerEvent.Clone} the fresh entity is typed {@code Player}. Never copy that
 * widening into a domain service. ⚠ {@code copy} must carry {@code BORN} or the next login after any
 * death rolls a second brilliance. ⚠ A new way to die needs a line in {@code onRespawn} -- the un-fire
 * there returns things BARE (soul 1, mind 0), never a value the lethal check would fire on.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see ApertureService
 * @see BodyService
 */
public final class PlayerDataService {

    private static final String VITAL_LOST = "guzhenren.item.gu.vital_lost";

    private PlayerDataService() {}

    public static void onJoin(ServerPlayer player) {
        player.getData(ModAttachments.EXHAUSTION_SEEN)[0] = player.getFoodData().getExhaustionLevel();
        if (!player.getData(ModAttachments.BORN)) onBirth(player);
        HealthService.refresh(player);
        AttackService.refresh(player);
        StaminaService.refresh(player);
    }

    public static void onBirth(Player player) {
        player.setData(ModAttachments.MIND, MindData.newborn());
        player.setData(ModAttachments.BORN, true);
    }

    public static void onSleepComplete(ServerPlayer player) {
        SoulService.refill(player);
        EssenceService.refill(player);
        MindService.onSleepComplete(player);
    }

    public static void onDeath(ServerPlayer player) {
        BodyService.setLifeForm(player, LifeForm.DEAD);
    }

    public static void onClone(Player from, Player to, boolean wasDeath, boolean keepInventory) {
        if (wasDeath && !keepInventory) {
            resetAll(to);
        } else {
            copy(from, to);
        }
        if (to instanceof ServerPlayer server) {
            HealthService.refresh(server);
            AttackService.refresh(server);
            StaminaService.refresh(server);
        }
    }

    public static void onRespawn(ServerPlayer player) {
        BodyService.setLifeForm(player, LifeForm.ALIVE);
        if (BodyService.get(player).isExhausted()) {
            BodyService.setLifespan(player, BodyData.DEFAULT_LIFESPAN);
        }
        if (SoulService.get(player).isCollapsed()) {
            SoulService.revive(player);
        }
        if (MindService.get(player).isOverflowing()) {
            MindService.empty(player);
        }
        BodyService.clearDeathQiDebt(player);
        QiService.set(player, QiKind.DEATH, 0L);
        StaminaService.refill(player);
    }

    public static void onVitalGuLost(ServerPlayer owner, ItemStack stack) {
        owner.sendSystemMessage(Component.translatable(VITAL_LOST, stack.getHoverName()));

        SoulService.setCurrent(owner, SoulService.get(owner).currentSoul() / 2L);
        for (WisdomType type : WisdomType.values()) {
            MindService.setCurrent(owner, type, MindService.current(owner, type) / 2L);
        }
        owner.hurt(ModDamageTypes.source(owner, ModDamageTypes.VITAL_GU_LOST), owner.getHealth() * 0.8F);

        //   TODO: a Gu cannot name the aperture that bound it, so PRIMARY.
        ApertureService.setPrimaryPath(owner, ApertureService.PRIMARY, null);
    }

    public static void copy(Player from, Player to) {
        to.setData(ModAttachments.APERTURE, from.getData(ModAttachments.APERTURE));
        to.setData(ModAttachments.APERTURE_STORAGE, from.getData(ModAttachments.APERTURE_STORAGE));
        to.setData(ModAttachments.BODY, from.getData(ModAttachments.BODY));
        to.setData(ModAttachments.SOUL, from.getData(ModAttachments.SOUL));
        to.setData(ModAttachments.STAMINA, from.getData(ModAttachments.STAMINA));
        to.setData(ModAttachments.PATH, from.getData(ModAttachments.PATH));
        to.setData(ModAttachments.QI, from.getData(ModAttachments.QI));
        to.setData(ModAttachments.STRENGTH, from.getData(ModAttachments.STRENGTH));
        to.setData(ModAttachments.MIND, from.getData(ModAttachments.MIND));
        to.setData(ModAttachments.NOURISH, from.getData(ModAttachments.NOURISH));
        to.setData(ModAttachments.BORN, from.getData(ModAttachments.BORN));
    }

    public static void resetAll(Player player) {
        player.setData(ModAttachments.APERTURE, ApertureData.DEFAULT);
        player.setData(ModAttachments.APERTURE_STORAGE, ApertureStorage.DEFAULT);
        player.setData(ModAttachments.SOUL, SoulData.DEFAULT);
        player.setData(ModAttachments.STAMINA, StaminaData.DEFAULT);
        player.setData(ModAttachments.PATH, PathData.DEFAULT);
        player.setData(ModAttachments.QI, QiData.DEFAULT);
        player.setData(ModAttachments.STRENGTH, StrengthData.DEFAULT);
        player.setData(ModAttachments.ESSENCE_CARRY, new float[ApertureData.MAX_APERTURES]);
        player.setData(ModAttachments.NOURISH, NourishData.DEFAULT);
        onBirth(player);

        player.setData(ModAttachments.BODY,
                BodyData.DEFAULT.withLastDayIndex(BodyService.get(player).lastDayIndex()));

        if (player instanceof ServerPlayer server) {
            HealthService.refresh(server);
            AttackService.refresh(server);
            StaminaService.refill(server);
        }
    }
}

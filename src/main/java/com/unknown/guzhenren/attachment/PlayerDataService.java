package com.unknown.guzhenren.attachment;

import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.QiData;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.registry.ModAttachments;
import com.unknown.guzhenren.registry.ModDamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//  Cross-system operations. Event handlers call these and stay dumb.
public final class PlayerDataService {

    private static final String VITAL_LOST = "guzhenren.item.gu.vital_lost";

    private PlayerDataService() {}

    //  First login ever, and nothing else -- vanilla gives no such signal, so BORN is the memory of it.
    //  ⚠ The max-health modifier is transient and does not survive a logout, so it is re-hung every login.
    public static void onJoin(ServerPlayer player) {
        if (!player.getData(ModAttachments.BORN)) onBirth(player);
        HealthService.refresh(player);
    }

    //  ⚠ Everything a player is dealt once, at birth. Brilliance is rolled HERE, not at awakening -- awaken must
    //  never touch it. A full reset is a rebirth, so resetAll calls this too. See CLAUDE.md "Birth".
    public static void onBirth(Player player) {
        player.setData(ModAttachments.MIND, MindData.newborn());
        player.setData(ModAttachments.BORN, true);
    }

    //  Completed night's sleep: soul + every aperture to full, thoughts restored -- see MindPool.slept.
    public static void onSleepComplete(ServerPlayer player) {
        SoulService.refill(player);
        EssenceService.refill(player);
        MindService.onSleepComplete(player);
    }

    //  What a fresh clone inherits. Death without keepInventory resets to mortal; else copy.
    public static void onClone(Player from, Player to, boolean wasDeath, boolean keepInventory) {
        if (wasDeath && !keepInventory) {
            resetAll(to);
        } else {
            copy(from, to);
        }
        //  A clone is a fresh entity with bare attributes, whatever its data says the rank is.
        if (to instanceof ServerPlayer server) HealthService.refresh(server);

        //  ⚠ resetAll flips sourceAwakened, which normally demands refreshCommands -- but vanilla's
        //  PlayerList.respawn already calls sendCommands on every respawn. Do not "fix" this.
    }

    //  Un-fire every lethal condition, or the player dies in a respawn loop. What ran out comes back
    //  bare, not full: the cap survives, the contents do not. See CLAUDE.md "Time, sleep, death".
    public static void onRespawn(ServerPlayer player) {
        if (BodyService.get(player).isExhausted()) {
            BodyService.setLifespan(player, BodyData.DEFAULT_LIFESPAN);
        }
        if (SoulService.get(player).isCollapsed()) {
            SoulService.revive(player);
        }
        if (MindService.get(player).isOverflowing()) {
            MindService.empty(player);
        }
    }

    //  His Vital Gu is gone. Everything he is takes the hit at once, and the caps are left alone.
    //  ⚠ 80% of the health he had LEFT can never kill by itself; halving a soul of 1 can, through the
    //  ordinary lethal check. ⚠ Plain uncolored chat -- nothing was refused, and he must not miss it.
    public static void onVitalGuLost(ServerPlayer owner, ItemStack stack) {
        owner.sendSystemMessage(Component.translatable(VITAL_LOST, stack.getHoverName()));

        SoulService.setCurrent(owner, SoulService.get(owner).currentSoul() / 2L);
        for (WisdomType type : WisdomType.values()) {
            MindService.setCurrent(owner, type, MindService.current(owner, type) / 2L);
        }
        owner.hurt(ModDamageTypes.source(owner, ModDamageTypes.VITAL_GU_LOST), owner.getHealth() * 0.8F);

        //  ⚠ The ONE place 主修流派 is cleared -- the Gu is dead wherever it was, and both starve walks
        //  land here. TODO(第二空窍): a Gu cannot say which aperture bound it, so this assumes PRIMARY.
        ApertureService.setPrimaryPath(owner, ApertureService.PRIMARY, null);
    }

    //  Death clones and non-death clones (End portal return, /debug respawn) alike.
    //  BORN travels with them, or the next login would roll a second Brilliance over the one he has.
    public static void copy(Player from, Player to) {
        to.setData(ModAttachments.APERTURE, from.getData(ModAttachments.APERTURE));
        to.setData(ModAttachments.APERTURE_STORAGE, from.getData(ModAttachments.APERTURE_STORAGE));
        to.setData(ModAttachments.BODY, from.getData(ModAttachments.BODY));
        to.setData(ModAttachments.SOUL, from.getData(ModAttachments.SOUL));
        to.setData(ModAttachments.PATH, from.getData(ModAttachments.PATH));
        to.setData(ModAttachments.QI, from.getData(ModAttachments.QI));
        to.setData(ModAttachments.STRENGTH, from.getData(ModAttachments.STRENGTH));
        to.setData(ModAttachments.MIND, from.getData(ModAttachments.MIND));
        to.setData(ModAttachments.BORN, from.getData(ModAttachments.BORN));
    }

    //  Player, not ServerPlayer, so onClone can reset the fresh clone entity as well as a live one.
    //  A reset is a rebirth: onBirth is what writes MIND here, and it rolls a fresh Brilliance.
    public static void resetAll(Player player) {
        player.setData(ModAttachments.APERTURE, ApertureData.DEFAULT);
        //  ⚠ The apertures are gone, so what they held goes with them -- the Gu inside are destroyed,
        //  exactly as the inventory is. Unawakened has nowhere to store anything.
        player.setData(ModAttachments.APERTURE_STORAGE, ApertureStorage.DEFAULT);
        player.setData(ModAttachments.SOUL, SoulData.DEFAULT);
        player.setData(ModAttachments.PATH, PathData.DEFAULT);
        player.setData(ModAttachments.QI, QiData.DEFAULT);
        player.setData(ModAttachments.STRENGTH, StrengthData.DEFAULT);
        player.setData(ModAttachments.ESSENCE_CARRY, new float[ApertureData.MAX_APERTURES]);
        onBirth(player);

        //  Keep the day anchor: resetting it would just be re-adopted on the next tick.
        player.setData(ModAttachments.BODY,
                BodyData.DEFAULT.withLastDayIndex(BodyService.get(player).lastDayIndex()));

        //  Back to 凡人, so back to 20 -- the aperture was emptied above, not written through ApertureService.
        if (player instanceof ServerPlayer server) HealthService.refresh(server);
    }
}

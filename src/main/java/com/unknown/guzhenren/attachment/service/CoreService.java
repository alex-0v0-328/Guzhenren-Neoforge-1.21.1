package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.core.GuRank;
import com.unknown.guzhenren.custom.enums.core.GuStage;
import com.unknown.guzhenren.custom.enums.core.GuTalent;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The core system: rank, stage, aptitude base, extreme physique, life state.
//
//  Every write goes through set(), which enforces the physique invariant and then re-clamps the
//  essence pool -- because the essence cap is derived from core, changing core silently moves it.
public final class CoreService {

    private CoreService() {}

    //  ---- read ----
    public static CoreData get(Player player) {return player.getData(ModAttachments.CORE);}
    public static GuTalent talent(Player player) {return get(player).talent();}

    //  ---- write ----
    public static void setRank(ServerPlayer p, GuRank rank) {set(p, get(p).withRank(rank));}
    public static void setStage(ServerPlayer p, GuStage stage) {set(p, get(p).withStage(stage));}
    public static void setBaseEssence(ServerPlayer p, int base) {set(p, get(p).withBaseEssence(base));}
    public static void addBaseEssence(ServerPlayer p, int delta) {setBaseEssence(p, get(p).baseEssence() + delta);}
    public static void setLifeState(ServerPlayer p, GuLifeState state) {set(p, get(p).withLifeState(state));}

    //  There is no tier field to assign, so "set the tier" can only mean "roll a base inside that
    //  tier's band". Landing on EXTREME lands on base 100, and set() then grants the physique.
    public static void setTalent(ServerPlayer p, GuTalent talent) {setBaseEssence(p, GuTalent.randomPercent(talent));}

    //  Positive delta always means "better". Each enum owns its own direction and its own edge -- see
    //  CLAUDE.md "Bounds", and GuTalent, whose direction is the backwards one.
    public static void shiftRank(ServerPlayer p, int delta) {setRank(p, get(p).rank().shift(delta));}
    public static void shiftStage(ServerPlayer p, int delta) {setStage(p, get(p).stage().shift(delta));}
    public static void shiftTalent(ServerPlayer p, int delta) {setTalent(p, get(p).talent().shift(delta));}

    public static void set(ServerPlayer player, CoreData core) {
        player.setData(ModAttachments.CORE, enforce(core));

        //  A rank or stage change moves the essence cap under the player's feet.
        EssenceService.clampToMax(player);
    }

    //  Physique and aptitude tier are two views of one fact, so granting or revoking a physique has
    //  to move the aptitude base with it, not just flip a field.
    public static void setExtremePhysique(ServerPlayer player, GuExtremePhysique physique) {
        CoreData core = get(player);

        if (physique == GuExtremePhysique.NONE) {
            //  Losing the physique drops you back to a rolled ordinary tier -- see the comment on
            //  GuTalent.randomNormalTalent, which exists for exactly this.
            if (core.isExtreme()) {
                core = core.withBaseEssence(GuTalent.randomPercent(GuTalent.randomNormalTalent()));
            }
            core = core.withExtremePhysique(GuExtremePhysique.NONE);
        } else {
            //  Holding a physique *is* the 十绝 tier, and that tier is aptitude base 100 by definition.
            core = core.withBaseEssence(CoreData.MAX_BASE).withExtremePhysique(physique);
        }

        set(player, core);
    }

    //  开窍. Rolls a tier by weight, a base inside that tier's band, and a physique if it came up 十绝.
    //  Opening the aperture *is* becoming a rank-one Gu Master, so rank and stage are set here too --
    //  which is what gives the player a non-zero essence cap to fill.
    public static void awaken(ServerPlayer player) {
        GuTalent talent = GuTalent.randomTalent();
        GuExtremePhysique physique = talent == GuTalent.EXTREME
                ? GuExtremePhysique.randomTenExtreme()
                : GuExtremePhysique.NONE;

        set(player, get(player)
                .withRank(GuRank.ONE)
                .withStage(GuStage.INIT)
                .withBaseEssence(GuTalent.randomPercent(talent))
                .withExtremePhysique(physique));

        //  A freshly opened aperture is a full one. set() above only clamps down to the new cap.
        EssenceService.refill(player);
    }

    //  The one invariant a raw CoreData cannot enforce for itself: a physique is held **iff** the
    //  aptitude tier is 十绝. This is what keeps `essence base set 100` and `set 50` both coherent.
    private static CoreData enforce(CoreData core) {
        boolean extreme = core.isExtreme();
        boolean hasPhysique = core.extremePhysique() != GuExtremePhysique.NONE;

        if (extreme && !hasPhysique) {
            return core.withExtremePhysique(GuExtremePhysique.randomTenExtreme());
        }
        if (!extreme && hasPhysique) {
            return core.withExtremePhysique(GuExtremePhysique.NONE);
        }
        return core;
    }
}

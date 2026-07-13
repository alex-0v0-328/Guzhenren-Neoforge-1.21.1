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

    //  Opening the aperture: roll a tier by weight (甲20 乙30 丙30 丁10 十绝10), roll an aptitude base
    //  inside that tier's band, and roll a physique if the tier came up 十绝.
    public static void awaken(ServerPlayer player) {
        GuTalent talent = GuTalent.randomTalent();
        GuExtremePhysique physique = talent == GuTalent.EXTREME
                ? GuExtremePhysique.randomTenExtreme()
                : GuExtremePhysique.NONE;

        set(player, get(player)
                .withBaseEssence(GuTalent.randomPercent(talent))
                .withExtremePhysique(physique));
    }

    //  The one invariant that a raw CoreData cannot enforce for itself:
    //  a physique is held if and only if the aptitude tier is 十绝 (i.e. aptitude base is 100).
    //  This is what keeps `/gzr core base set 100` and `/gzr core base set 50` both coherent.
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

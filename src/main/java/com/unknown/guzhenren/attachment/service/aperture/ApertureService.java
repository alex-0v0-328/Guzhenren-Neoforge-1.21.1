package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The aperture (空窍) system. Every write goes through set(): it enforces the physique invariant, and
//  Aperture's own ctor re-clamps the essence pool against the cap the new rank/stage/base implies.
//  Index defaults to PRIMARY everywhere; 第二空窍 has no mechanic yet, only a place to live.
public final class ApertureService {

    private ApertureService() {}

    public static final int PRIMARY = ApertureData.PRIMARY;

    //  ---- read ----
    public static ApertureData get(Player p) {return p.getData(ModAttachments.APERTURE);}
    public static Aperture aperture(Player p) {return get(p).primary();}
    public static Aperture aperture(Player p, int i) {return get(p).get(i);}
    public static boolean isAwakened(Player p) {return get(p).isAwakened();}
    public static Talent talent(Player p) {return aperture(p).talent();}
    public static Rank rank(Player p) {return aperture(p).rank();}
    public static Stage stage(Player p) {return aperture(p).stage();}

    //  ---- write, on the primary aperture ----
    public static void setRank(ServerPlayer p, Rank v) {set(p, PRIMARY, aperture(p).withRank(v));}
    public static void setStage(ServerPlayer p, Stage v) {set(p, PRIMARY, aperture(p).withStage(v));}
    public static void setState(ServerPlayer p, ApertureState v) {set(p, PRIMARY, aperture(p).withState(v));}
    public static void addBaseEssence(ServerPlayer p, int d) {setBaseEssence(p, aperture(p).baseEssence() + d);}

    //  No tier field: "set the tier" rolls a base inside its band. EXTREME hits 100; set() grants the physique.
    public static void setTalent(ServerPlayer p, Talent v) {setBaseEssence(p, Talent.randomPercent(v));}

    //  Positive delta = better. Each enum owns its direction and its edge; Talent's runs backwards.
    public static void shiftRank(ServerPlayer p, int d) {setRank(p, aperture(p).rank().shift(d));}
    public static void shiftStage(ServerPlayer p, int d) {setStage(p, aperture(p).stage().shift(d));}
    public static void shiftTalent(ServerPlayer p, int d) {setTalent(p, aperture(p).talent().shift(d));}

    //  ⚠ A live aperture is never base 0 -- that value belongs to Aperture.NONE alone. 未开窍 is an
    //  empty list, and the way back to it is /gzr reset, not an essence base of zero.
    public static void setBaseEssence(ServerPlayer p, int v) {
        set(p, PRIMARY, aperture(p).withBaseEssence(Math.clamp(v, Aperture.MIN_BASE, Aperture.MAX_BASE)));
    }

    //  Physique and aptitude tier are two views of one fact -- granting or revoking one moves the base.
    public static void setExtremePhysique(ServerPlayer player, ExtremePhysique physique) {
        Aperture aperture = aperture(player);

        if (physique == ExtremePhysique.NONE) {
            //  Back to a rolled ordinary tier -- Talent.randomNormalTalent exists for exactly this.
            if (aperture.isExtreme()) {
                aperture = aperture.withBaseEssence(Talent.randomPercent(Talent.randomNormalTalent()));
            }
            aperture = aperture.withExtremePhysique(ExtremePhysique.NONE);
        } else {
            //  Holding a physique *is* the 十绝 tier, and that tier is base 100 by definition.
            aperture = aperture.withBaseEssence(Aperture.MAX_BASE).withExtremePhysique(physique);
        }

        set(player, PRIMARY, aperture);
    }

    //  开窍. Appends an aperture -- the caller is what refuses a holder who is full. See CmdAperture.
    public static void awaken(ServerPlayer player) {
        store(player, get(player).opened(enforce(Aperture.opened())));
    }

    //  Replaces an aperture that exists; an index nobody opened is a no-op, never a grow.
    public static void set(ServerPlayer player, int index, Aperture aperture) {
        store(player, get(player).with(index, enforce(aperture)));
    }

    private static void store(ServerPlayer p, ApertureData data) {p.setData(ModAttachments.APERTURE, data);}

    //  The invariant Aperture cannot enforce alone (the fix rolls a die): a physique is held **iff** 十绝.
    private static Aperture enforce(Aperture aperture) {
        boolean extreme = aperture.isExtreme();
        boolean hasPhysique = aperture.extremePhysique() != ExtremePhysique.NONE;

        if (extreme && !hasPhysique) {
            return aperture.withExtremePhysique(ExtremePhysique.randomTenExtreme());
        }
        if (!extreme && hasPhysique) {
            return aperture.withExtremePhysique(ExtremePhysique.NONE);
        }
        return aperture;
    }
}

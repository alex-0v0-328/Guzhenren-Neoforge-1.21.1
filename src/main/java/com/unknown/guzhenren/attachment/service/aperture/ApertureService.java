package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

//  The aperture (空窍) system. Index defaults to PRIMARY -- a second aperture has a place to live, no
//  mechanic yet.
//  ⚠ Every write goes through set(): it enforces the physique invariant, and Aperture's ctor re-clamps.
public final class ApertureService {

    private ApertureService() {}

    public static final int PRIMARY = ApertureData.PRIMARY;

    //  A 十绝 physique's innate 道痕 / 碎屑, split evenly across its 天赋流派 (总数, so two paths get half each).
    public static final long TALENT_MARK_TOTAL = 10L;
    public static final long TALENT_SPECK_TOTAL = 1000L;

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

    //  ---- 主修 / 辅修 ----
    //  Primary is DERIVED from the Vital Gu, but stored, because ApertureStorage is not synced and this
    //  record is -- storing the path IS the sync. ⚠ No-ops when unchanged, the HealthService.refresh
    //  idiom: setVital runs on every menu click and every day tick, and each write pushes a packet.
    public static void setPrimaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.primaryPath() == v) return;
        set(p, index, aperture.withPrimaryPath(v));
    }

    //  ⚠ The player's own choice, so it is NOT recomputed from anything. Aperture's ctor drops it when it
    //  would equal 主修 -- binding a Vital Gu of the same path is what makes that fire.
    public static void setSecondaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.secondaryPath() == v) return;
        set(p, index, aperture.withSecondaryPath(v));
    }

    //  Positive delta = better. Each enum owns its direction and its edge; Talent's runs backwards.
    public static void shiftRank(ServerPlayer p, int d) {setRank(p, aperture(p).rank().shift(d));}
    public static void shiftStage(ServerPlayer p, int d) {setStage(p, aperture(p).stage().shift(d));}
    public static void shiftTalent(ServerPlayer p, int d) {setTalent(p, aperture(p).talent().shift(d));}

    //  ⚠ A live aperture is never base 0 -- that value belongs to Aperture.NONE alone. Unawakened is an
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
            //  Holding a physique *is* the Ten Extreme tier, and that tier is base 100 by definition.
            aperture = aperture.withBaseEssence(Aperture.MAX_BASE).withExtremePhysique(physique);
        }

        set(player, PRIMARY, aperture);
    }

    //  Awakening (开窍). Appends an aperture -- the caller is what refuses a full holder. See CmdAperture.
    public static void awaken(ServerPlayer player) {
        ExtremePhysique before = aperture(player).extremePhysique();
        store(player, get(player).opened(enforce(Aperture.opened())));
        reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    //  Replaces an aperture that exists; an index nobody opened is a no-op, never a grow.
    public static void set(ServerPlayer player, int index, Aperture aperture) {
        ExtremePhysique before = index == PRIMARY ? aperture(player).extremePhysique() : null;
        store(player, get(player).with(index, enforce(aperture)));
        if (index == PRIMARY) reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    //  ⚠ Every aperture write funnels through here -- which is why the rank-driven max health hangs off it.
    //  Same cross-domain convention as reconcileTalentPaths: the trigger's service calls the target's.
    private static void store(ServerPlayer p, ApertureData data) {
        p.setData(ModAttachments.APERTURE, data);
        HealthService.refresh(p);
    }

    //  The invariant Aperture cannot enforce alone (the fix rolls a die): a physique is held **iff** Extreme.
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

    //  A 十绝 physique grants innate path 道痕/碎屑; changing it revokes the old grant and lays down the new.
    //  Read the physique AFTER enforce -- enforce is what actually rolls or clears it. Read is why not derived.
    //  Convention: a cross-domain "X grants Y" rule lives in the service that owns the trigger (physique is
    //  空窍's, so it lives here) and calls the target domain's service. See CLAUDE.md "Invariants".
    //  TODO(refactor): if such grant rules reach 2-3, extract a coordinator; one rule does not earn it.
    private static void reconcileTalentPaths(ServerPlayer player, ExtremePhysique before, ExtremePhysique after) {
        if (before == after) return;
        grantTalentPaths(player, before, -1);
        grantTalentPaths(player, after, 1);
    }

    //  ⚠ Granted marks carry no source tag -- they merge into the path's total ("natural" origin), so a
    //  revoke is a plain subtraction that clamps at 0. No need to track which marks the physique gave.
    //  ⚠ addMark silently refuses a featured path (气道 today; 力道/宙道 by design) -- 大力真武体's 力道 and
    //  荒古老月体's 宙道 grant fine now, but vanish the day either lights up. See CLAUDE.md "Featured body paths".
    private static void grantTalentPaths(ServerPlayer player, ExtremePhysique physique, int sign) {
        List<GuPath> paths = physique.getTalentPaths();
        if (paths.isEmpty()) return;
        long mark = sign * (TALENT_MARK_TOTAL / paths.size());
        long speck = sign * (TALENT_SPECK_TOTAL / paths.size());
        for (GuPath path : paths) {
            PathService.addMark(player, path, mark);
            PathService.addSpeck(player, path, speck);
        }
    }
}

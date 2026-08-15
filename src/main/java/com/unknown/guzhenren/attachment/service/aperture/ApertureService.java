package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.body.HealthService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The only writer of the Aperture [空窍] attachment: awakening [开窍], rank, stage, talent and paths.
 *
 * <p>Static service over the {@code aperture_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Every write routes through {@code store}, which also fires
 * {@link HealthService#refresh} so max health never lags a rank change. {@code reconcileTalentPaths}
 * is the one place {@code aperture/} writes {@code body/} -- it grants/revokes the ten-extreme talent
 * specks and the human qi.
 *
 * <p>⚠ The physique-and-talent invariant is enforced here ({@code enforce}) rather than in the record,
 * because repairing it rolls a die and a compact constructor has to stay a pure function. ⚠ The
 * "after" physique must be read AFTER {@code enforce} -- reading it before sees {@code NONE} and the
 * talent grant silently never lands. ⚠ {@code awaken} does NOT refuse an awakened holder -- it
 * appends; the caller gates. ⚠ {@code reconcileTalentPaths} is one of the two existing cross-domain
 * grants; a third one is the threshold to extract a coordinator (the {@code TODO(refactor)} below).
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see ApertureData
 * @see EssenceService
 */
public final class ApertureService {

    private ApertureService() {}

    public static final int PRIMARY = ApertureData.PRIMARY;

    public static final long TALENT_SPECK_TOTAL = 5000L;
    public static final long TALENT_HUMAN_QI = 100L;

    public static ApertureData get(Player p) {return p.getData(ModAttachments.APERTURE);}
    public static Aperture aperture(Player p) {return get(p).primary();}
    public static Aperture aperture(Player p, int i) {return get(p).get(i);}
    public static boolean isAwakened(Player p) {return get(p).isAwakened();}
    public static Talent talent(Player p) {return aperture(p).talent();}
    public static Rank rank(Player p) {return aperture(p).rank();}
    public static Stage stage(Player p) {return aperture(p).stage();}

    public static void setRank(ServerPlayer p, Rank v) {set(p, PRIMARY, aperture(p).withRank(v));}
    public static void setStage(ServerPlayer p, Stage v) {set(p, PRIMARY, aperture(p).withStage(v));}
    public static void addBaseEssence(ServerPlayer p, int d) {setBaseEssence(p, aperture(p).baseEssence() + d);}

    public static void setTalent(ServerPlayer p, Talent v) {setBaseEssence(p, Talent.randomPercent(v));}

    public static void setPrimaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.primaryPath() == v) return;
        set(p, index, aperture.withPrimaryPath(v));
    }

    public static void setSecondaryPath(ServerPlayer p, int index, @Nullable GuPath v) {
        Aperture aperture = aperture(p, index);
        if (aperture.secondaryPath() == v) return;
        set(p, index, aperture.withSecondaryPath(v));
    }

    public static void shiftRank(ServerPlayer p, int d) {setRank(p, aperture(p).rank().shift(d));}
    public static void shiftStage(ServerPlayer p, int d) {setStage(p, aperture(p).stage().shift(d));}
    public static void shiftTalent(ServerPlayer p, int d) {setTalent(p, aperture(p).talent().shift(d));}

    public static void setBaseEssence(ServerPlayer p, int v) {
        set(p, PRIMARY, aperture(p).withBaseEssence(Math.clamp(v, Aperture.MIN_BASE, Aperture.MAX_BASE)));
    }

    public static void setExtremePhysique(ServerPlayer player, ExtremePhysique physique) {
        Aperture aperture = aperture(player);

        if (physique == ExtremePhysique.NONE) {
            if (aperture.isExtreme()) {
                aperture = aperture.withBaseEssence(Talent.randomPercent(Talent.randomNormalTalent()));
            }
            aperture = aperture.withExtremePhysique(ExtremePhysique.NONE);
        } else {
            aperture = aperture.withBaseEssence(Aperture.MAX_BASE).withExtremePhysique(physique);
        }

        set(player, PRIMARY, aperture);
    }

    public static void awaken(ServerPlayer player) {open(player, Aperture.opened());}

    public static void awaken(ServerPlayer player, int baseEssence) {
        open(player, Aperture.openedAt(baseEssence));
    }

    private static void open(ServerPlayer player, Aperture aperture) {
        ExtremePhysique before = aperture(player).extremePhysique();
        store(player, get(player).opened(enforce(aperture)));
        reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    public static void set(ServerPlayer player, int index, Aperture aperture) {
        ExtremePhysique before = index == PRIMARY ? aperture(player).extremePhysique() : null;
        store(player, get(player).with(index, enforce(aperture)));
        if (index == PRIMARY) reconcileTalentPaths(player, before, aperture(player).extremePhysique());
    }

    private static void store(ServerPlayer p, ApertureData data) {
        p.setData(ModAttachments.APERTURE, data);
        HealthService.refresh(p);
    }

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

    //    TODO(refactor): extract a coordinator once cross-domain grant rules reach 3; TWO exist today.
    private static void reconcileTalentPaths(ServerPlayer player, ExtremePhysique before, ExtremePhysique after) {
        if (before == after) return;
        grantTalentPaths(player, before, -1);
        grantTalentPaths(player, after, 1);
    }

    private static void grantTalentPaths(ServerPlayer player, ExtremePhysique physique, int sign) {
        List<GuPath> paths = physique.getTalentPaths();
        if (paths.isEmpty()) return;

        QiService.add(player, QiKind.HUMAN, sign * TALENT_HUMAN_QI);
        long speck = sign * (TALENT_SPECK_TOTAL / paths.size());
        for (GuPath path : paths) PathService.addSpeck(player, path, MarkTag.NATURAL, speck);
    }
}

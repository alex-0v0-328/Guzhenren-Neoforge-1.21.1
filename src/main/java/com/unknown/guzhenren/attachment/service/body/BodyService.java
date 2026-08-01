package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The body [肉身] system: life state, life form, lifespan. One in-game day: age +1, lifespan -1.
public final class BodyService {

    private BodyService() {}

    //  ---- read ----
    //  Overworld, never the player's own level -- the Nether and the End have a fixed day-time.
    //  ⚠ TICKS_PER_DAY used to live here and moved to Ticks.DAY (2026-08-01) -- one home for every unit
    //  of time, since items and events read them too. Do not declare a second 24000 anywhere.
    public static long dayIndex(MinecraftServer server) {
        return server.overworld().getDayTime() / Ticks.DAY;
    }

    public static BodyData get(Player p) {return p.getData(ModAttachments.BODY);}
    public static LifeState lifeState(Player p) {return get(p).lifeState();}
    public static LifeForm lifeForm(Player p) {return get(p).lifeForm();}
    public static Race race(Player p) {return get(p).race();}

    //  ---- write ----
    public static void setLifeState(ServerPlayer p, LifeState v) {store(p, get(p).withLifeState(v));}
    public static void setLifeForm(ServerPlayer p, LifeForm v) {store(p, get(p).withLifeForm(v));}
    public static void setAge(ServerPlayer p, long v) {store(p, get(p).withAge(v));}
    public static void addAge(ServerPlayer p, long d) {setAge(p, get(p).age() + d);}
    public static void setLifespan(ServerPlayer p, long v) {store(p, get(p).withLifespan(v));}
    public static void addLifespan(ServerPlayer p, long d) {setLifespan(p, get(p).lifespan() + d);}
    private static void store(ServerPlayer p, BodyData data) {p.setData(ModAttachments.BODY, data);}

    //region Race [种族]
    //  ⚠ Everyone is BORN Race.HUMAN [人族] -- onBirth says nothing about race, because HUMAN is simply
    //  the default. Becoming a Variant Human [异人] happens here and nowhere else, by command or item.
    //  ⚠ Intra-domain, not cross-domain: race sits on BodyData and the marks on PathData, both 肉身.
    public static void setRace(ServerPlayer player, Race race) {
        Race current = race(player);
        if (current == race) return;

        revokeTalent(player, current);
        store(player, get(player).withRace(race));
        grantTalent(player, race);
    }

    //  Ten marks on its talent path, and Master [大师] in it.
    private static void grantTalent(ServerPlayer player, Race race) {
        GuPath path = race.talentPath();
        if (path == null) return;

        PathService.setMark(player, path, MarkTag.RACE, Race.TALENT_MARKS);
        //  ⚠⚠ RAISES only, never lowers -- an attainment is a GRADE, so there is no way to tell what the
        //  race gave from what he earned. That asymmetry is why revokeTalent does not touch it at all.
        if (PathService.attainment(player, path).ordinal() < Race.TALENT_ATTAINMENT.ordinal()) {
            PathService.setAttainment(player, path, Race.TALENT_ATTAINMENT);
        }
    }

    //  ⚠ Zeroes the RACE tag alone, which is EXACTLY what was laid down -- marks he earned himself on the
    //  same path sit under their own tags and are untouched. That is what the tag exists for.
    private static void revokeTalent(ServerPlayer player, Race race) {
        GuPath path = race.talentPath();
        if (path != null) PathService.setMark(player, path, MarkTag.RACE, 0L);
    }
    //endregion

    //region Death Qi [死气] debt
    //  ⚠ One write, two facts: lifespan drops and the tally rises together, or a cure could refund
    //  lifespan Death Qi never took. Life Qi [生气] hands back three quarters and clears the tally.
    public static void drainByDeathQi(ServerPlayer player, long years) {
        BodyData body = get(player);
        store(player, body.withLifespan(body.lifespan() - years)
                .withDeathQiLifespanLost(body.deathQiLifespanLost() + years));
    }

    //  Returns what was handed back, so the caller can say so. Floors, so a debt of 1 refunds nothing.
    public static long refundDeathQiDebt(ServerPlayer player, int numerator, int denominator) {
        BodyData body = get(player);
        long refund = body.deathQiLifespanLost() * numerator / denominator;
        store(player, body.withLifespan(body.lifespan() + refund).withDeathQiLifespanLost(0L));
        return refund;
    }

    //  ⚠ A respawn must clear it: the debt outlived the death that settled it, and Life Qi would
    //  otherwise refund lifespan against a curse the player no longer carries.
    public static void clearDeathQiDebt(ServerPlayer p) {store(p, get(p).withDeathQiLifespanLost(0L));}
    //endregion

    //  Bill every whole day since last billing. A stored day index, not a countdown -- idempotent, relog-safe.
    //  ⚠ Returns the days billed, because it is the only place that knows: anything else on the day
    //  rollover (a Gu's hunger) would otherwise need a second copy of lastDayIndex.
    public static long tickAging(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0L;

        long today = dayIndex(server);
        BodyData body = get(player);

        //  First tick ever, or time ran backwards (/time set): re-anchor rather than hand back lifespan.
        if (body.lastDayIndex() == BodyData.UNTRACKED || today < body.lastDayIndex()) {
            store(player, body.withLastDayIndex(today));
            return 0L;
        }

        long elapsed = today - body.lastDayIndex();
        if (elapsed == 0L) return 0L;

        store(player, body.aged(elapsed, today));
        return elapsed;
    }
}

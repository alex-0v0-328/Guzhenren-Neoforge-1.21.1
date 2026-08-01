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

public final class BodyService {

    private BodyService() {}

    public static long dayIndex(MinecraftServer server) {
        return server.overworld().getDayTime() / Ticks.DAY;
    }

    public static BodyData get(Player p) {return p.getData(ModAttachments.BODY);}
    public static LifeState lifeState(Player p) {return get(p).lifeState();}
    public static LifeForm lifeForm(Player p) {return get(p).lifeForm();}
    public static Race race(Player p) {return get(p).race();}

    public static void setLifeState(ServerPlayer p, LifeState v) {store(p, get(p).withLifeState(v));}
    public static void setLifeForm(ServerPlayer p, LifeForm v) {store(p, get(p).withLifeForm(v));}
    public static void setAge(ServerPlayer p, long v) {store(p, get(p).withAge(v));}
    public static void addAge(ServerPlayer p, long d) {setAge(p, get(p).age() + d);}
    public static void setLifespan(ServerPlayer p, long v) {store(p, get(p).withLifespan(v));}
    public static void addLifespan(ServerPlayer p, long d) {setLifespan(p, get(p).lifespan() + d);}
    private static void store(ServerPlayer p, BodyData data) {p.setData(ModAttachments.BODY, data);}

    //region Race [种族]
    public static void setRace(ServerPlayer player, Race race) {
        Race current = race(player);
        if (current == race) return;

        revokeTalent(player, current);
        store(player, get(player).withRace(race));
        grantTalent(player, race);
    }

    private static void grantTalent(ServerPlayer player, Race race) {
        GuPath path = race.talentPath();
        if (path == null) return;

        PathService.setMark(player, path, MarkTag.RACE, Race.TALENT_MARKS);
        if (PathService.attainment(player, path).ordinal() < Race.TALENT_ATTAINMENT.ordinal()) {
            PathService.setAttainment(player, path, Race.TALENT_ATTAINMENT);
        }
    }

    private static void revokeTalent(ServerPlayer player, Race race) {
        GuPath path = race.talentPath();
        if (path != null) PathService.setMark(player, path, MarkTag.RACE, 0L);
    }
    //endregion

    //region Death Qi [死气] debt
    public static void drainByDeathQi(ServerPlayer player, long years) {
        BodyData body = get(player);
        store(player, body.withLifespan(body.lifespan() - years)
                .withDeathQiLifespanLost(body.deathQiLifespanLost() + years));
    }

    public static long refundDeathQiDebt(ServerPlayer player, int numerator, int denominator) {
        BodyData body = get(player);
        long refund = body.deathQiLifespanLost() * numerator / denominator;
        store(player, body.withLifespan(body.lifespan() + refund).withDeathQiLifespanLost(0L));
        return refund;
    }

    public static void clearDeathQiDebt(ServerPlayer p) {store(p, get(p).withDeathQiLifespanLost(0L));}
    //endregion

    public static long tickAging(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0L;

        long today = dayIndex(server);
        BodyData body = get(player);

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

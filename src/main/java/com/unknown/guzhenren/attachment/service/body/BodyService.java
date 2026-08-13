package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.Ticks;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * The only writer of Body [肉身] state, and the home of every life-form [生命形态] transition.
 *
 * <p>⚠ {@code tickAging} returns how many days it just billed, and that count is often far more than
 * one -- an offline stretch or a {@code /time} jump arrives as a single call.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class BodyService {

    private BodyService() {}

    public static long dayIndex(MinecraftServer server) {
        return server.overworld().getDayTime() / Ticks.DAY;
    }

    public static BodyData get(Player p) {return p.getData(ModAttachments.BODY);}
    public static LifeForm lifeForm(Player p) {return get(p).lifeForm();}
    public static boolean isZombie(Player p) {return get(p).isZombie();}
    public static boolean isHalfZombie(Player p) {return get(p).isHalfZombie();}
    public static Race race(Player p) {return get(p).race();}
    public static long now(Player p) {return p.level().getGameTime();}

    public static void setAge(ServerPlayer p, long v) {store(p, get(p).withAge(v));}
    public static void addAge(ServerPlayer p, long d) {setAge(p, get(p).age() + d);}
    public static void setLifespan(ServerPlayer p, long v) {store(p, get(p).withLifespan(v));}
    public static void addLifespan(ServerPlayer p, long d) {setLifespan(p, get(p).lifespan() + d);}
    private static void store(ServerPlayer p, BodyData data) {p.setData(ModAttachments.BODY, data);}

    //region Life form [生命形态] -- 生 / 死 / 僵 / 半生半僵
    public static void setLifeForm(ServerPlayer player, LifeForm form) {
        BodyData body = get(player);
        if (body.lifeForm() == form) return;

        BodyData turned = body.withLifeForm(form);
        store(player, form.isZombie() ? turned.withLifespan(BodyData.ZOMBIE_LIFESPAN) : turned);
        AttackService.refresh(player);
    }

    public static void enterHalfZombie(ServerPlayer player, int tier, int durationTicks) {
        store(player, get(player)
                .withLifeForm(LifeForm.HALF_ZOMBIE)
                .withHalfZombieEndTick(now(player) + durationTicks)
                .withZombieTier(tier));
        AttackService.refresh(player);
    }

    public static void turnZombie(ServerPlayer player, int tier) {
        store(player, get(player)
                .withLifeForm(LifeForm.ZOMBIE)
                .withLifespan(BodyData.ZOMBIE_LIFESPAN)
                .withZombieTier(tier));
        AttackService.refresh(player);
    }

    public static boolean wouldRelapse(Player p) {return get(p).withinRelapseWindow(now(p));}
    public static long halfZombieTicksLeft(Player p) {return get(p).halfZombieTicksLeft(now(p));}
    public static boolean halfZombieRanOut(Player p) {return get(p).halfZombieRanOut(now(p));}
    //endregion

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
        PathService.shiftAttainment(player, path, Race.TALENT_SHIFT);
    }

    private static void revokeTalent(ServerPlayer player, Race race) {
        GuPath path = race.talentPath();
        if (path == null) return;

        PathService.setMark(player, path, MarkTag.RACE, 0L);
        PathService.shiftAttainment(player, path, -Race.TALENT_SHIFT);
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
        BodyData body = bankHastenedTime(player);

        if (body.lastDayIndex() == BodyData.UNTRACKED || today < body.lastDayIndex()) {
            store(player, body.withLastDayIndex(today));
            return 0L;
        }

        long elapsed = today - body.lastDayIndex();
        if (elapsed == 0L) return 0L;

        store(player, body.lifeForm().ages()
                ? agedAfterHastening(body, elapsed, today)
                : body.withLastDayIndex(today));
        return elapsed;
    }

    /**
     * ⚠ Years of his OWN life spent since 寿元 was last billed, and it goes negative while he carries
     * credit. Never clamp it: the display subtracts it, and the clamp is what would make it stand still.
     */
    public static double yearsSinceBilled(Player player) {
        BodyData body = get(player);
        if (body.lastDayIndex() == BodyData.UNTRACKED) return 0.0D;

        return yearsSinceBilled(player.level().getDayTime(), body.lastDayIndex(), body.hastenedParts());
    }

    public static double yearsSinceBilled(long dayTime, long lastDayIndex, long hastenedParts) {
        long unbilled = Math.max(0L, dayTime / Ticks.DAY - lastDayIndex);
        double parts = Ticks.DAY * (double) TimeFlowService.PARTS_PER_TICK;
        return unbilled + dayTime % Ticks.DAY / (double) Ticks.DAY - hastenedParts / parts;
    }

    //region 宙道 [Time Path] -- the days he lived through without spending them
    private static BodyData bankHastenedTime(ServerPlayer player) {
        BodyData body = get(player);
        long parts = TimeFlowService.skipped(player, Ticks.SECOND);
        if (parts <= 0L || !body.lifeForm().ages()) return body;

        BodyData banked = body.withHastenedParts(body.hastenedParts() + parts);
        store(player, banked);
        return banked;
    }

    /**
     * ⚠ The count of days is billed to 寿元 alone and is NEVER what this returns to its caller -- three
     * Gu-hunger walks read that count, and forgiving it there would freeze every Gu's bar instead.
     */
    private static BodyData agedAfterHastening(BodyData body, long elapsed, long today) {
        long perDay = Ticks.DAY * TimeFlowService.PARTS_PER_TICK;
        long free = Math.min(elapsed, body.hastenedParts() / perDay);
        return body.aged(elapsed - free, today).withHastenedParts(body.hastenedParts() - free * perDay);
    }
    //endregion
}

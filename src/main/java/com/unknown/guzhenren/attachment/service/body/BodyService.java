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
 * <p>Static service over the {@code body_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Owns two clocks with two anchors: {@code tickAging} keeps {@code lastDayIndex}
 * and returns DAYS for the Gu-hunger walks; {@code tickLifespan} keeps {@code lastBilledTick} and
 * bills lifespan. {@code setLifeForm} no-ops when unchanged so the zombie's 寿元=1 is never re-written
 * over a recovering zombie.
 *
 * <p>⚠ {@code tickAging} returns how many days it just billed, and that count is often far more than
 * one -- an offline stretch or a {@code /time} jump arrives as a single call; the return drives three
 * day-clock walks, so swallowing it would starve every Gu at once. ⚠ 寿元 is SPENT through
 * {@link TimeFlowService#perStep} -- hand-rolling the rate here once made it run BACKWARDS, into a
 * pure longevity buff. Only {@code InfoModel} may read {@code rate()} to print it. ⚠ The anchor is
 * {@code dayTime}, not {@code gameTime}, so {@code /time add} still ages him; time running backwards
 * re-anchors and bills nothing -- one {@code <} is the whole guard, on BOTH clocks. ⚠ Every caller
 * speaks YEARS; only this file knows parts exist.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see TimeFlowService
 * @see AttackService
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

    private static void store(ServerPlayer p, BodyData data) {p.setData(ModAttachments.BODY, data);}

    //region 寿元与年龄 [lifespan and age] -- ⚠ every caller speaks YEARS; only this file knows about parts
    public static void setAge(ServerPlayer p, long years) {store(p, get(p).withAgeParts(BodyData.parts(years)));}
    public static void setLifespan(ServerPlayer p, long years) {
        store(p, get(p).withLifespanParts(BodyData.parts(years)));
    }

    public static void addAge(ServerPlayer p, long years) {
        store(p, get(p).withAgeParts(get(p).ageParts() + BodyData.parts(years)));
    }
    public static void addLifespan(ServerPlayer p, long years) {
        store(p, get(p).withLifespanParts(get(p).lifespanParts() + BodyData.parts(years)));
    }
    //endregion

    //region Life form [生命形态] -- 生 / 死 / 僵 / 半生半僵
    public static void setLifeForm(ServerPlayer player, LifeForm form) {
        BodyData body = get(player);
        if (body.lifeForm() == form) return;

        BodyData turned = body.withLifeForm(form);
        store(player, form.isZombie() ? turned.withLifespanParts(BodyData.parts(BodyData.ZOMBIE_LIFESPAN)) : turned);
        AttackService.refresh(player);
    }

    public static void revive(ServerPlayer player) {
        store(player, get(player).revived());
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
                .withLifespanParts(BodyData.parts(BodyData.ZOMBIE_LIFESPAN))
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
        store(player, body.withLifespanParts(body.lifespanParts() - BodyData.parts(years))
                .withDeathQiLifespanLost(body.deathQiLifespanLost() + years));
    }

    public static double refundDeathQiDebt(ServerPlayer player, int numerator, int denominator) {
        BodyData body = get(player);
        long refundParts = BodyData.parts(body.deathQiLifespanLost()) * numerator / denominator;
        store(player, body.withLifespanParts(body.lifespanParts() + refundParts)
                .withDeathQiLifespanLost(0L));
        return (double) refundParts / BodyData.PARTS_PER_YEAR;
    }

    public static void clearDeathQiDebt(ServerPlayer p) {store(p, get(p).withDeathQiLifespanLost(0L));}
    //endregion

    /**
     * ⚠ This counts DAYS for the three Gu-hunger walks and nothing else -- 寿元 left it for
     * {@code tickLifespan}, which bills every heartbeat instead of once a day.
     */
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

        store(player, body.withLastDayIndex(today));
        return elapsed;
    }

    //region 寿元的钟 -- billed on the heartbeat, because 宙道 changes how fast he spends it
    /**
     * ⚠ The anchor is the world's {@code dayTime}, not {@code gameTime}, so {@code /time add} still ages
     * him. Time running backwards re-anchors and bills nothing, the same guard {@code tickAging} carries.
     */
    public static void tickLifespan(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        long now = server.overworld().getDayTime();
        BodyData body = get(player);

        if (body.lastBilledTick() == BodyData.UNTRACKED || now < body.lastBilledTick()) {
            store(player, body.withLastBilledTick(now));
            return;
        }
        if (!body.lifeForm().ages()) {
            store(player, body.withLastBilledTick(now));
            return;
        }
        long lived = TimeFlowService.perStep(player, elapsedParts(now - body.lastBilledTick()));
        if (lived <= 0L) return;

        store(player, body.lived(lived, now));
    }

    /**
     * ☠ 寿元 is SPENT, so it goes through {@code perStep} like every other thing he spends -- hastened
     * means FASTER. Hand-rolling the rate here once made it run backwards, into a pure longevity buff.
     */
    public static long elapsedParts(long elapsedTicks) {
        return Math.max(0L, elapsedTicks) * BodyData.PARTS_PER_TICK;
    }
    //endregion
}

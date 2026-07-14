package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The body (肉身) system: 生死僵, 凡/仙, 寿元. One in-game day: age +1, lifespan -1.
public final class BodyService {

    private BodyService() {}

    //  The world clock lives here: a day is what 寿元 is counted in. EssenceService borrows it to turn
    //  a per-day regen rate into a per-tick one.
    public static final int TICKS_PER_DAY = 24000;

    //  ---- read ----
    //  Overworld, never the player's own level -- the Nether and the End have a fixed day-time.
    public static long dayIndex(MinecraftServer server) {
        return server.overworld().getDayTime() / TICKS_PER_DAY;
    }

    public static BodyData get(Player p) {return p.getData(ModAttachments.BODY);}
    public static LifeState lifeState(Player p) {return get(p).lifeState();}
    public static LifeForm lifeForm(Player p) {return get(p).lifeForm();}

    //  ---- write ----
    public static void setLifeState(ServerPlayer p, LifeState v) {store(p, get(p).withLifeState(v));}
    public static void setLifeForm(ServerPlayer p, LifeForm v) {store(p, get(p).withLifeForm(v));}
    public static void setAge(ServerPlayer p, long v) {store(p, get(p).withAge(v));}
    public static void addAge(ServerPlayer p, long d) {setAge(p, get(p).age() + d);}
    public static void setLifespan(ServerPlayer p, long v) {store(p, get(p).withLifespan(v));}
    public static void addLifespan(ServerPlayer p, long d) {setLifespan(p, get(p).lifespan() + d);}
    private static void store(ServerPlayer p, BodyData data) {p.setData(ModAttachments.BODY, data);}

    //  Bill every whole day since last billing. A stored day index, not a countdown -- idempotent, relog-safe.
    public static void tickAging(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        long today = dayIndex(server);
        BodyData body = get(player);

        //  First tick ever, or time ran backwards (/time set): re-anchor rather than hand back lifespan.
        if (body.lastDayIndex() == BodyData.UNTRACKED || today < body.lastDayIndex()) {
            store(player, body.withLastDayIndex(today));
            return;
        }

        long elapsed = today - body.lastDayIndex();
        if (elapsed == 0L) return;

        store(player, body.aged(elapsed, today));
    }
}

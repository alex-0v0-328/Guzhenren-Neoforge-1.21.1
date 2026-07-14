package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The lifespan (寿元) system. One in-game day: age +1, lifespan -1.
public final class LifespanService {

    private LifespanService() {}

    //  ---- read ----
    //  Overworld, never the player's own level: the Nether and the End have a fixed day-time, so a
    //  player who moved there would stop aging.
    public static long dayIndex(MinecraftServer server) {
        return server.overworld().getDayTime() / EssenceService.TICKS_PER_DAY;
    }
    public static LifespanData get(Player player) {return player.getData(ModAttachments.LIFESPAN);}

    //  ---- write ----
    public static void setAge(ServerPlayer p, long v) {store(p, get(p).withAge(v));}
    public static void addAge(ServerPlayer p, long delta) {setAge(p, get(p).age() + delta);}
    public static void setLifespan(ServerPlayer p, long v) {store(p, get(p).withLifespan(v));}
    public static void addLifespan(ServerPlayer p, long delta) {setLifespan(p, get(p).lifespan() + delta);}

    //  Bill every whole in-game day since the last billing. A stored day index, not a countdown: a
    //  double tick cannot double-charge, and ten days offline are billed as exactly ten years.
    public static void tickAging(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        long today = dayIndex(server);
        LifespanData data = get(player);

        //  First tick ever: adopt today, charge nothing.
        if (data.lastDayIndex() == LifespanData.UNTRACKED) {
            store(player, data.withLastDayIndex(today));
            return;
        }

        long elapsed = today - data.lastDayIndex();

        //  Time ran backwards (/time set). Re-anchor rather than hand back lifespan.
        if (elapsed < 0L) {
            store(player, data.withLastDayIndex(today));
            return;
        }
        if (elapsed == 0L) return;

        store(player, new LifespanData(data.age() + elapsed, data.lifespan() - elapsed, today));
    }

    private static void store(ServerPlayer p, LifespanData data) {p.setData(ModAttachments.LIFESPAN, data);}
}

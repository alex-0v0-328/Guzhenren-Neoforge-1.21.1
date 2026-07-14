package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.path.PathData;
import com.unknown.guzhenren.attachment.data.path.PathEntry;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The path (流派) system. Attainment and marks are independent: two setters, no shared logic.
public final class PathService {

    private PathService() {}

    //  ---- read ----
    //  entry() is never null -- PathData prunes defaults, so an untouched path reads back as one.
    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuPathAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}
    public static long mark(Player p, GuPath path) {return entry(p, path).mark();}

    //  ---- write ----
    public static void addMark(ServerPlayer p, GuPath path, long delta) {setMark(p, path, mark(p, path) + delta);}
    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    //  Whole tiers, clamped at 无 and 无上大宗师. Marks do not move: a promotion that gifted marks
    //  would quietly couple the two.
    public static void shiftAttainment(ServerPlayer p, GuPath path, int delta) {
        setAttainment(p, path, attainment(p, path).shift(delta));
    }

    public static void setMark(ServerPlayer p, GuPath path, long v) {
        store(p, get(p).with(path, entry(p, path).withMark(v)));
    }

    public static void setAttainment(ServerPlayer p, GuPath path, GuPathAttainment attainment) {
        store(p, get(p).with(path, entry(p, path).withAttainment(attainment)));
    }
}

package com.unknown.guzhenren.attachment.service;

import com.unknown.guzhenren.attachment.data.PathData;
import com.unknown.guzhenren.attachment.data.PathEntry;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The path (流派) system: 气道 大宗师 道痕1000.
//
//  Attainment and marks are deliberately independent -- grinding marks does not promote you, and a
//  promotion does not gift you marks. So they get two setters and no shared logic.
public final class PathService {

    private PathService() {}

    //  ---- read ----
    //  entry() is never null: PathData prunes default entries, so an untouched path reads back as
    //  the default rather than as a hole in the map.
    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuPathAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}
    public static long mark(Player p, GuPath path) {return entry(p, path).mark();}

    //  ---- write ----
    public static void addMark(ServerPlayer p, GuPath path, long delta) {setMark(p, path, mark(p, path) + delta);}
    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    //  Promote or demote by whole tiers, clamped at 无 and 无上大宗师. Marks do not move -- the two are
    //  independent by design, and a promotion that gifted marks would quietly couple them.
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

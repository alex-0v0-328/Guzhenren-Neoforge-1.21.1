package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The path (流派) system. Attainment, marks and specks are independent -- no mark<->speck conversion.
//  ⚠ A featured path's mark/speck ARE its sub-system's total (气道 = QiData): derived here, not writable.
public final class PathService {

    private PathService() {}

    //  ---- read ----
    //  entry() is never null -- PathData prunes defaults, so an untouched path reads back as one.
    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}

    //  ⚠ Derived for a featured path. Reading PathData directly gives 0 for 气道 -- come through here.
    public static long mark(Player p, GuPath path) {
        return path == GuPath.QI ? QiService.total(p) : entry(p, path).mark();
    }

    //  气道 has no speck source yet, so it reads 0 (PathData zeroes it). A featured path that DOES
    //  earn specks adds a dispatch here, mirroring mark(). See CLAUDE.md "Featured body paths".
    public static long speck(Player p, GuPath path) {return entry(p, path).speck();}

    //  The display view: every path with something to show, 气道's marks filled in from QiData.
    public static Map<GuPath, PathEntry> visibleEntries(Player player) {
        Map<GuPath, PathEntry> view = new EnumMap<>(GuPath.class);
        view.putAll(get(player).entries());

        long qi = QiService.total(player);
        if (qi > 0L || view.containsKey(GuPath.QI)) {
            view.put(GuPath.QI, entry(player, GuPath.QI).withMark(qi));
        }
        return view;
    }

    //  ---- write ----
    public static void addMark(ServerPlayer p, GuPath path, long delta) {setMark(p, path, mark(p, path) + delta);}
    public static void addSpeck(ServerPlayer p, GuPath path, long d) {setSpeck(p, path, speck(p, path) + d);}
    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    //  Whole tiers, clamped at 无 and 无上大宗师. Marks do not move -- a promotion gifting marks would couple them.
    public static void shiftAttainment(ServerPlayer p, GuPath path, int delta) {
        setAttainment(p, path, attainment(p, path).shift(delta));
    }

    //  ⚠ A featured path refuses: its mark/speck come from its sub-system. CmdPath says so out loud;
    //  these guards are what stop an item from silently trying.
    public static void setMark(ServerPlayer p, GuPath path, long v) {
        if (path.isFeatured()) return;
        store(p, get(p).with(path, entry(p, path).withMark(v)));
    }

    public static void setSpeck(ServerPlayer p, GuPath path, long v) {
        if (path.isFeatured()) return;
        store(p, get(p).with(path, entry(p, path).withSpeck(v)));
    }

    public static void setAttainment(ServerPlayer p, GuPath path, GuAttainment attainment) {
        store(p, get(p).with(path, entry(p, path).withAttainment(attainment)));
    }
}

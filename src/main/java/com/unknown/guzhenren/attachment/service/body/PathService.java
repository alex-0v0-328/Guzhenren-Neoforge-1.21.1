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

//  The path (流派) system. Attainment and marks are independent: two setters, no shared logic.
//  ⚠ 气道 is the exception: its marks ARE QiData's total -- derived here, and not writable. Its
//  attainment is an ordinary stored field like every other path's. See CLAUDE.md "Qi".
public final class PathService {

    private PathService() {}

    //  ---- read ----
    //  entry() is never null -- PathData prunes defaults, so an untouched path reads back as one.
    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}

    //  ⚠ The one derived path value. Reading PathData directly gives 0 for 气道 -- come through here.
    public static long mark(Player p, GuPath path) {
        return path == GuPath.QI ? QiService.total(p) : entry(p, path).mark();
    }

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
    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    //  Whole tiers, clamped at 无 and 无上大宗师. Marks do not move -- a promotion gifting marks would couple them.
    public static void shiftAttainment(ServerPlayer p, GuPath path, int delta) {
        setAttainment(p, path, attainment(p, path).shift(delta));
    }

    //  ⚠ 气道 refuses: its marks come from QiService. CmdPath says so out loud; this guard is what
    //  stops an item from silently trying.
    public static void setMark(ServerPlayer p, GuPath path, long v) {
        if (path == GuPath.QI) return;
        store(p, get(p).with(path, entry(p, path).withMark(v)));
    }

    public static void setAttainment(ServerPlayer p, GuPath path, GuAttainment attainment) {
        store(p, get(p).with(path, entry(p, path).withAttainment(attainment)));
    }
}

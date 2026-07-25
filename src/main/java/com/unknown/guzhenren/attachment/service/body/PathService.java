package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.PathData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The path [流派] system. Attainment, marks and specks are independent -- no mark<->speck conversion.
//  ⚠ Every write names a source tag; PathData drops one that does not fit the path. See MarkTag.
public final class PathService {

    private PathService() {}

    //  ---- read ----
    //  entry() is never null -- PathData prunes defaults, so an untouched path reads back as one.
    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}
    public static long mark(Player p, GuPath path) {return entry(p, path).mark();}
    public static long speck(Player p, GuPath path) {return entry(p, path).speck();}
    public static long mark(Player p, GuPath path, MarkTag t) {return entry(p, path).mark(t);}
    public static long speck(Player p, GuPath path, MarkTag t) {return entry(p, path).speck(t);}

    //  The display view. ⚠ Nothing is derived any more: the Qi Path reads out of PathData like every
    //  other path now that its types are tags, so there is no sub-system total to splice in.
    public static Map<GuPath, PathEntry> visibleEntries(Player player) {return get(player).entries();}

    //  ---- write ----
    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    public static void setMark(ServerPlayer p, GuPath path, MarkTag tag, long v) {
        store(p, get(p).with(path, entry(p, path).withMark(tag, v)));
    }
    public static void setSpeck(ServerPlayer p, GuPath path, MarkTag tag, long v) {
        store(p, get(p).with(path, entry(p, path).withSpeck(tag, v)));
    }
    public static void addMark(ServerPlayer p, GuPath path, MarkTag tag, long delta) {
        setMark(p, path, tag, mark(p, path, tag) + delta);
    }
    public static void addSpeck(ServerPlayer p, GuPath path, MarkTag tag, long delta) {
        setSpeck(p, path, tag, speck(p, path, tag) + delta);
    }

    //  Whole tiers, clamped at both ends. Marks do not move -- a promotion gifting marks would couple them.
    public static void shiftAttainment(ServerPlayer p, GuPath path, int delta) {
        setAttainment(p, path, attainment(p, path).shift(delta));
    }

    public static void setAttainment(ServerPlayer p, GuPath path, GuAttainment attainment) {
        store(p, get(p).with(path, entry(p, path).withAttainment(attainment)));
    }

    //  Spend one tag's worth, all or nothing. Qi leaving the body to become a material is the first
    //  caller-to-be; addMark is the return trip. ⚠ Ratio unplanned, item nonexistent -- invent neither.
    public static boolean consume(ServerPlayer player, GuPath path, MarkTag tag, long amount) {
        if (amount <= 0L) return true;
        long current = mark(player, path, tag);
        if (current < amount) return false;
        setMark(player, path, tag, current - amount);
        return true;
    }
}

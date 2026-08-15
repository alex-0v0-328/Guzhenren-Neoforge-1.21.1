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

/**
 * The only writer of Path [流派] progress: attainment, Dao marks [道痕] and specks [碎屑].
 *
 * <p>Static service over the {@code path_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Every write names a {@link MarkTag}, and the record drops a tag owned by a
 * different path -- this is the one door that keeps a foreign tag out of a path's entry.
 *
 * <p>⚠ Every write names a {@link MarkTag}, and one owned by a different path is dropped SILENTLY by
 * the record. A write with the wrong tag looks like it worked and did nothing at all -- there is no
 * log, no exception. ⚠ The command path writes {@code NATURAL} and takes NO tag argument -- 力道's
 * four and {@code RACE} are earned, and a hand-written one cannot be told from them; do not re-add a
 * tag argument or a gate enum. ⚠ {@code attainment} is a GRADE, so a race grant that shifts it up is
 * never "set" -- it MOVES, and the revoke shifts back down; nothing can tell a granted master from an
 * earned one.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see PathData
 * @see PathEntry
 */
public final class PathService {

    private PathService() {}

    public static PathData get(Player player) {return player.getData(ModAttachments.PATH);}
    public static PathEntry entry(Player p, GuPath path) {return get(p).get(path);}
    public static GuAttainment attainment(Player p, GuPath path) {return entry(p, path).attainment();}
    public static long mark(Player p, GuPath path) {return entry(p, path).markTotal();}
    public static long speck(Player p, GuPath path) {return entry(p, path).speckTotal();}
    public static long mark(Player p, GuPath path, MarkTag t) {return entry(p, path).mark(t);}
    public static long speck(Player p, GuPath path, MarkTag t) {return entry(p, path).speck(t);}

    public static Map<GuPath, PathEntry> visibleEntries(Player player) {return get(player).entries();}

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

    public static void shiftAttainment(ServerPlayer p, GuPath path, int delta) {
        setAttainment(p, path, attainment(p, path).shift(delta));
    }

    public static void setAttainment(ServerPlayer p, GuPath path, GuAttainment attainment) {
        store(p, get(p).with(path, entry(p, path).withAttainment(attainment)));
    }

    public static boolean consume(ServerPlayer player, GuPath path, MarkTag tag, long amount) {
        if (amount <= 0L) return true;
        long current = mark(player, path, tag);
        if (current < amount) return false;
        setMark(player, path, tag, current - amount);
        return true;
    }
}

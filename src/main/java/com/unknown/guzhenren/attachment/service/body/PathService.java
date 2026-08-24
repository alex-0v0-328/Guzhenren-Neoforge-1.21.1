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
 * The only writer of Path [流派] progress: attainment and Dao marks [道痕].
 *
 * <p>Static service over the {@code path_data} attachment; reads take {@link Player}, writes take
 * {@link ServerPlayer}. Every write names a {@link MarkTag}; the command path writes {@code NATURAL}
 * and takes NO tag argument. {@code RACE} and {@code EXTREME_PHYSIQUE} marks are written by their
 * respective systems; do not re-add a tag argument or a gate enum. ⚠ {@code attainment} is a GRADE,
 * so a race grant that shifts it up is never "set" -- it MOVES, and the revoke shifts back down; nothing
 * can tell a granted master from an earned one.
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
    public static long mark(Player p, GuPath path, MarkTag t) {return entry(p, path).mark(t);}

    public static Map<GuPath, PathEntry> visibleEntries(Player player) {return get(player).entries();}

    private static void store(ServerPlayer p, PathData data) {p.setData(ModAttachments.PATH, data);}

    public static void setMark(ServerPlayer p, GuPath path, MarkTag tag, long v) {
        store(p, get(p).with(path, entry(p, path).withMark(tag, v)));
    }
    public static void addMark(ServerPlayer p, GuPath path, MarkTag tag, long delta) {
        setMark(p, path, tag, mark(p, path, tag) + delta);
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

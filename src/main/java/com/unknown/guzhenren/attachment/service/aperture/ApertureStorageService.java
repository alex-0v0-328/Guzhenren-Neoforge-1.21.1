package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.item.GuItem;
import com.unknown.guzhenren.item.gu.MortalGuItem;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The only writer of what an Aperture [空窍] holds, including the Vital Gu [本命蛊] bound to each.
 *
 * <p>Static service over the {@code aperture_storage} attachment; reads take {@link Player}, writes
 * take {@link ServerPlayer}. {@code setVital} also rewrites the aperture's primary path via
 * {@link ApertureService#setPrimaryPath} -- binding a Gu IS what sets 主修 [primary path], because the
 * store is not synced and the aperture is.
 *
 * <p>⚠ It reaches into the {@code item/} package on purpose ({@link GuItem}), against this project's
 * usual dependency direction: binding a Vital Gu has to read that Gu's declared path. Do not "fix"
 * those imports. ⚠ Writes NEVER go through {@code ApertureService.store} -- {@link
 * com.unknown.guzhenren.attachment.service.body.BodyHealthService#refresh} hangs off that, and moving one
 * item must not recompute max health. ⚠ {@code setVital} runs on every menu click and every day tick,
 * and {@code setPrimaryPath} no-ops when unchanged, so leaving that call in is free and removing it
 * loses the path on rebind.
 *
 * @author Alex
 * @version 1.0.0
 * @see ApertureService
 * @see ApertureStorageTick
 * @since 1.0.0
 */

public final class ApertureStorageService {

    private ApertureStorageService() {}

    public static final int MAX_LOAD = 256;

    public static @NotNull ApertureStorage get(@NotNull Player p) {return p.getData(ModAttachments.APERTURE_STORAGE);}
    public static @NotNull List<ItemStack> items(@NotNull Player p, int aperture) {return get(p).get(aperture);}
    public static @NotNull List<ItemStack> page(@NotNull Player p, int aperture, int from, int size) {
        return get(p).page(aperture, from, size);
    }
    public static boolean pageMatches(@NotNull Player p, int aperture, int from, @NotNull List<ItemStack> page) {
        return get(p).matchesPage(aperture, from, page);
    }
    public static int count(@NotNull Player p, int aperture) {return get(p).count(aperture);}
    public static @NotNull ItemStack vital(@NotNull Player p, int aperture) {return get(p).getVital(aperture);}
    public static int load(@NotNull Player p, int aperture) {return load(p, get(p), aperture);}

    public static boolean set(@NotNull ServerPlayer p, int aperture, @NotNull List<ItemStack> items) {
        ApertureStorage current = get(p);
        ApertureStorage next = current.with(aperture, items);
        if (exceedsLoad(load(p, current, aperture), load(p, next, aperture))) return false;

        p.setData(ModAttachments.APERTURE_STORAGE, next);
        return true;
    }

    public static boolean setVital(@NotNull ServerPlayer p, int aperture, @NotNull ItemStack stack) {
        ApertureStorage current = get(p);
        ApertureStorage next = current.withVital(aperture, stack);
        if (exceedsLoad(load(p, current, aperture), load(p, next, aperture))) return false;

        p.setData(ModAttachments.APERTURE_STORAGE, next);
        if (stack.getItem() instanceof GuItem gu) ApertureService.setPrimaryPath(p, aperture, gu.path());
        return true;
    }

    public static boolean setPage(@NotNull ServerPlayer p, int aperture, int from, @NotNull List<ItemStack> page) {
        ApertureStorage current = get(p);
        ApertureStorage next = current.withPage(aperture, from, page);
        if (exceedsLoad(load(p, current, aperture), load(p, next, aperture))) return false;

        p.setData(ModAttachments.APERTURE_STORAGE, next);
        return true;
    }

    private static int load(Player p, ApertureStorage storage, int aperture) {
        Rank holder = ApertureService.aperture(p, aperture).rank();
        int total = 0;
        if (aperture >= 0 && aperture < storage.byAperture().size()) {
            total += load(holder, storage.byAperture().get(aperture));
        }
        if (aperture >= 0 && aperture < storage.vital().size()) {
            total += cost(holder, storage.vital().get(aperture));
        }
        return total;
    }

    private static int load(Rank holder, List<ItemStack> stacks) {
        int total = 0;
        for (ItemStack stack : stacks) total += cost(holder, stack);
        return total;
    }

    private static boolean exceedsLoad(int current, int next) {return next > Math.max(MAX_LOAD, current);}

    private static int cost(Rank holder, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof MortalGuItem gu)) return 0;

        int gap = gu.rank().ordinal() - holder.ordinal();
        int perItem = gap < 0 ? 1 : gap == 0 ? 2 : 2 << gap;
        return perItem * stack.getCount();
    }
}

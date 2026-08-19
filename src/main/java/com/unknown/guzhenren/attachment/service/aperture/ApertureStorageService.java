package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.item.GuItem;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
 * those imports. ⚠ Writes NEVER go through {@link ApertureService#store} -- {@link HealthService#refresh}
 * hangs off that, and moving one item must not recompute max health. ⚠ {@code setVital} runs on every
 * menu click and every day tick, and {@code setPrimaryPath} no-ops when unchanged, so leaving that
 * call in is free and removing it loses the path on rebind.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see ApertureService
 * @see ApertureStorageTick
 */
public final class ApertureStorageService {

    private ApertureStorageService() {}

    public static ApertureStorage get(Player p) {return p.getData(ModAttachments.APERTURE_STORAGE);}
    public static List<ItemStack> items(Player p, int aperture) {return get(p).get(aperture);}
    public static List<ItemStack> page(Player p, int aperture, int from, int size) {
        return get(p).page(aperture, from, size);
    }
    public static boolean pageMatches(Player p, int aperture, int from, List<ItemStack> page) {
        return get(p).matchesPage(aperture, from, page);
    }
    public static int count(Player p, int aperture) {return get(p).count(aperture);}
    public static ItemStack vital(Player p, int aperture) {return get(p).getVital(aperture);}

    public static void set(ServerPlayer p, int aperture, List<ItemStack> items) {
        p.setData(ModAttachments.APERTURE_STORAGE, get(p).with(aperture, items));
    }

    public static void setVital(ServerPlayer p, int aperture, ItemStack stack) {
        p.setData(ModAttachments.APERTURE_STORAGE, get(p).withVital(aperture, stack));
        if (stack.getItem() instanceof GuItem gu) ApertureService.setPrimaryPath(p, aperture, gu.path());
    }

    public static void setPage(ServerPlayer p, int aperture, int from, List<ItemStack> page) {
        p.setData(ModAttachments.APERTURE_STORAGE, get(p).withPage(aperture, from, page));
    }
}

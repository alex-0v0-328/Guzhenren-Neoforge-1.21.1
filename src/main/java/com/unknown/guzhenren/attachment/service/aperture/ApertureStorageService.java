package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.item.GuItem;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ApertureStorageService {

    private ApertureStorageService() {}

    public static ApertureStorage get(Player p) {return p.getData(ModAttachments.APERTURE_STORAGE);}
    public static List<ItemStack> items(Player p, int aperture) {return get(p).get(aperture);}
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
        List<ItemStack> all = new ArrayList<>(items(p, aperture));
        while (all.size() < from + page.size()) all.add(ItemStack.EMPTY);
        for (int i = 0; i < page.size(); i++) all.set(from + i, page.get(i));
        set(p, aperture, all);
    }
}

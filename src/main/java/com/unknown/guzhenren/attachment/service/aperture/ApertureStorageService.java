package com.unknown.guzhenren.attachment.service.aperture;

import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import com.unknown.guzhenren.item.GuItem;
import com.unknown.guzhenren.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//  What an aperture holds. The only writer, as every service is.
//  ⚠ Deliberately NOT routed through ApertureService.store() -- HealthService.refresh hangs off that,
//  and moving one item must not recompute max health.
public final class ApertureStorageService {

    private ApertureStorageService() {}

    //  ---- read ----
    public static ApertureStorage get(Player p) {return p.getData(ModAttachments.APERTURE_STORAGE);}
    public static List<ItemStack> items(Player p, int aperture) {return get(p).get(aperture);}
    public static int count(Player p, int aperture) {return get(p).count(aperture);}
    public static ItemStack vital(Player p, int aperture) {return get(p).getVital(aperture);}

    //  ---- write ----
    public static void set(ServerPlayer p, int aperture, List<ItemStack> items) {
        p.setData(ModAttachments.APERTURE_STORAGE, get(p).with(aperture, items));
    }

    //  The Vital Gu slot: one Gu, outside the paged store  CLAUDE.md "Aperture storage".
    //  ⚠ Writes the primary path too. This is the ONE choke point for the slot, and the primary is
    //  nothing but the bound Gu's path -- a caller told to remember both would eventually forget one.
    //  ⚠ The `item/**` import this costs is the same documented exception ApertureStorageTick carries:
    //  reading a Gu's DECLARED path is not calling item behaviour  CLAUDE.md "Extending & compat".
    public static void setVital(ServerPlayer p, int aperture, ItemStack stack) {
        p.setData(ModAttachments.APERTURE_STORAGE, get(p).withVital(aperture, stack));
        //  ⚠ Emptying the slot does NOT clear the primary path. A Vital Gu has to be held to be used, so
        //  the slot stands empty most of the time -- the primary follows the MARK, and only its death
        //  clears it (PlayerDataService.onVitalGuLost). Binding a different Gu is what overwrites it.
        if (stack.getItem() instanceof GuItem gu) ApertureService.setPrimaryPath(p, aperture, gu.path());
    }

    //  Writes one page's worth back at a fixed offset; everything outside the window is untouched.
    //  ⚠ `from` is an index into the aperture's flat list, not a slot -- callers pass page * pageSize.
    public static void setPage(ServerPlayer p, int aperture, int from, List<ItemStack> page) {
        List<ItemStack> all = new ArrayList<>(items(p, aperture));
        //  Pad with holes so the window can be written even past the current end.
        while (all.size() < from + page.size()) all.add(ItemStack.EMPTY);
        for (int i = 0; i < page.size(); i++) all.set(from + i, page.get(i));
        //  ⚠ Only trailing holes are trimmed (ApertureStorage's ctor) -- slot positions stay put.
        set(p, aperture, all);
    }
}

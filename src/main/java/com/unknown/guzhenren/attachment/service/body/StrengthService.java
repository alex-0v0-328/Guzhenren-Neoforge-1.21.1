package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The Strength Path [力道], both branches: which beast strengths a body took, and how many layers of
//  each Human Jun strength. ⚠ Stored and shown only -- no attribute, no combat effect. Titles today.
public final class StrengthService {

    private StrengthService() {}

    //  ---- read ----
    public static StrengthData get(Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(Player p, BeastStrength b) {return get(p).has(b);}
    public static int boarCount(Player p) {return get(p).boarCount();}
    public static int humanStrength(Player p, HumanStrength k) {return get(p).humanStrengthCount(k);}
    public static boolean hasBranch(Player p, StrengthBranch b) {return get(p).hasBranch(b);}

    //  ---- write ----
    public static void grant(ServerPlayer p, BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(ServerPlayer p, BeastStrength b) {store(p, get(p).without(b));}
    public static void clear(ServerPlayer p) {store(p, StrengthData.DEFAULT);}
    private static void store(ServerPlayer p, StrengthData d) {p.setData(ModAttachments.STRENGTH, d);}

    //  Both run past 120 as one-liners, so they sit below the tight group rather than lose their names.
    public static void setHumanStrength(ServerPlayer p, HumanStrength k, int v) {
        store(p, get(p).withHumanStrength(k, v));
    }
    public static void addHumanStrength(ServerPlayer p, HumanStrength k, int d) {
        setHumanStrength(p, k, humanStrength(p, k) + d);
    }
}

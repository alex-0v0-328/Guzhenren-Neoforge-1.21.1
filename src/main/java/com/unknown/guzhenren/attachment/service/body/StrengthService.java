package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The Strength Path [力道], both branches: which beast strengths a body took, and how many of each
//  Jun strength. ⚠ Stored and shown only -- no attribute, no combat effect. The branches are titles today.
public final class StrengthService {

    private StrengthService() {}

    //  ---- read ----
    public static StrengthData get(Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(Player p, BeastStrength b) {return get(p).has(b);}
    public static int boarCount(Player p) {return get(p).boarCount();}
    public static int jun(Player p, JunStrength k) {return get(p).junCount(k);}
    public static boolean hasBranch(Player p, StrengthBranch b) {return get(p).hasBranch(b);}

    //  ---- write ----
    public static void grant(ServerPlayer p, BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(ServerPlayer p, BeastStrength b) {store(p, get(p).without(b));}
    public static void setJun(ServerPlayer p, JunStrength k, int v) {store(p, get(p).withJun(k, v));}
    public static void addJun(ServerPlayer p, JunStrength k, int d) {setJun(p, k, jun(p, k) + d);}
    public static void clear(ServerPlayer p) {store(p, StrengthData.DEFAULT);}
    private static void store(ServerPlayer p, StrengthData d) {p.setData(ModAttachments.STRENGTH, d);}
}

package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//  The Strength Path (力道) system: the beast strengths a body has taken, and nothing else yet.
//  ⚠ Stored and shown only -- no attribute, no combat effect. The path's own mechanics are unspecced.
public final class StrengthService {

    private StrengthService() {}

    //  ---- read ----
    public static StrengthData get(Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(Player p, BeastStrength b) {return get(p).has(b);}
    public static int boarCount(Player p) {return get(p).boarCount();}

    //  ---- write ----
    public static void grant(ServerPlayer p, BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(ServerPlayer p, BeastStrength b) {store(p, get(p).without(b));}
    public static void clear(ServerPlayer p) {store(p, StrengthData.DEFAULT);}
    private static void store(ServerPlayer p, StrengthData d) {p.setData(ModAttachments.STRENGTH, d);}
}

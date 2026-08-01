package com.unknown.guzhenren.attachment.service.body;

import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class StrengthService {

    private StrengthService() {}

    public static StrengthData get(Player p) {return p.getData(ModAttachments.STRENGTH);}
    public static boolean has(Player p, BeastStrength b) {return get(p).has(b);}
    public static int humanStrength(Player p, HumanStrength k) {return get(p).humanStrengthCount(k);}
    public static boolean hasBranch(Player p, StrengthBranch b) {return get(p).hasBranch(b);}

    public static void grant(ServerPlayer p, BeastStrength b) {store(p, get(p).with(b));}
    public static void revoke(ServerPlayer p, BeastStrength b) {store(p, get(p).without(b));}
    public static void clear(ServerPlayer p) {store(p, StrengthData.DEFAULT);}
    private static void store(ServerPlayer p, StrengthData d) {
        p.setData(ModAttachments.STRENGTH, d);
        AttackService.refresh(p);
    }

    public static void setHumanStrength(ServerPlayer p, HumanStrength k, int v) {
        store(p, get(p).withHumanStrength(k, v));
    }
    public static void addHumanStrength(ServerPlayer p, HumanStrength k, int d) {
        setHumanStrength(p, k, humanStrength(p, k) + d);
    }
}

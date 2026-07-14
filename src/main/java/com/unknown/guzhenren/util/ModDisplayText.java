package com.unknown.guzhenren.util;

import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.LifespanData;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

//  Every phrase that describes a cultivator, in one place. The HUD builds these on the client,
//  /gzr info the same ones on the server -- sharing the builders is what stops the two drifting.
//
//  Values only ("86 [ 14岁 ]"), never labels: the HUD says 寿元 and the command says 玩家寿元, so each
//  caller wraps the value in its own key. No color anywhere -- see CLAUDE.md "Color".
public final class ModDisplayText {

    private ModDisplayText() {}

    private static final String GAP = "  ";

    //  一转巅峰 / Rank I Peak. The separator lives in the lang key -- zh joins, en needs a space.
    public static MutableComponent realm(CoreData core) {
        return Component.translatable("guzhenren.display.realm",
                Component.translatable(core.rank().getTranslationKey()),
                Component.translatable(core.stage().getTranslationKey()));
    }

    //  甲等资质 [ 太日阳莽体 ] -- the bracket only shows up for a 十绝 physique holder.
    public static MutableComponent talent(CoreData core) {
        MutableComponent line = Component.translatable(core.talent().getTranslationKey());
        if (core.extremePhysique() == GuExtremePhysique.NONE) return line;

        return line.append(" ").append(Component.translatable("guzhenren.display.physique",
                Component.translatable(core.extremePhysique().getTranslationKey())));
    }

    //  86 [ 14岁 ]
    public static MutableComponent lifespan(LifespanData data) {
        return Component.translatable("guzhenren.display.lifespan", data.lifespan(), data.age());
    }

    //  一转巅峰  甲等资质 [ 太日阳莽体 ] -- the HUD's first line.
    public static MutableComponent realmAndTalent(CoreData core) {
        return realm(core).append(GAP).append(talent(core));
    }

    //  800/800. A raw String, not a Component -- drawn straight into the HUD bar.
    public static String pool(long current, long max) {return current + "/" + max;}
}

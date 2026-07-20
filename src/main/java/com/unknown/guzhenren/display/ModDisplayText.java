package com.unknown.guzhenren.display;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

//  Every phrase that describes a cultivator, shared by the HUD and /gzr info so they can't drift.
//  Values only, never labels; each caller wraps in its own key. See CLAUDE.md "Color".
public final class ModDisplayText {

    private ModDisplayText() {}

    private static final String GAP = "  ";

    //  一转巅峰 / Rank I Peak. The separator lives in the lang key -- zh joins, en needs a space.
    public static MutableComponent realm(Aperture aperture) {
        return Component.translatable("guzhenren.display.realm",
                Component.translatable(aperture.rank().getTranslationKey()),
                Component.translatable(aperture.stage().getTranslationKey()));
    }

    //  甲等资质 [ 太日阳莽体 ] -- the bracket only shows up for a Ten Extreme physique holder.
    public static MutableComponent talent(Aperture aperture) {
        MutableComponent line = Component.translatable(aperture.talent().getTranslationKey());
        if (aperture.extremePhysique() == ExtremePhysique.NONE) return line;

        return line.append(" ").append(Component.translatable("guzhenren.display.physique",
                Component.translatable(aperture.extremePhysique().getTranslationKey())));
    }

    //  一转人道蛊虫 / Rank I Human Path Gu. Separator lives in the lang key -- zh joins, en spaces.
    public static MutableComponent guLine(Rank rank, GuPath path, String kindKey) {
        return Component.translatable("guzhenren.display.gu_line",
                Component.translatable(rank.getTranslationKey()),
                Component.translatable(path.getTranslationKey()),
                Component.translatable(kindKey));
    }

    //  野生·黑豕蛊 / Wild Black Boar Gu -- one key for every Gu that has a wild form.
    public static MutableComponent wild(Component name) {return Component.translatable("guzhenren.display.wild", name);}

    //  86 [ 14岁 ]
    public static MutableComponent lifespan(BodyData body) {
        return Component.translatable("guzhenren.display.lifespan", body.lifespan(), body.age());
    }

    //  一转巅峰  甲等资质 [ 太日阳莽体 ] -- the HUD's first line.
    public static MutableComponent realmAndTalent(Aperture a) {return realm(a).append(GAP).append(talent(a));}

    //  800/800. A raw String, not a Component -- drawn straight into the HUD bar.
    public static String pool(long current, long max) {return current + "/" + max;}

    //  一猪之力 / 两猪之力. Keyed by the count: zh wants 一/两, en wants One/Two and a plural.
    //  ⚠ One key per count -- a third beast strength needs a third key, never a concatenation.
    public static Component boarStrength(int count) {
        return Component.translatable("guzhenren.display.boar_strength." + count);
    }

    //  一斤之力 .. 九斤之力. Keyed by kind AND count -- a later 十斤 gets its own nine keys, not a multiplier.
    public static Component junStrength(JunStrength kind, int count) {
        return Component.translatable("guzhenren.display.jun_strength." + kind.getSerializedName() + "." + count);
    }

    //  The aptitude base read in tenths: 89 -> 八成九, 80 -> 八成, 100 -> 十成. English has no such reading,
    //  so it spells the number out (89 -> Eighty Nine) -- tens and units are separate words, hence two key sets.
    public static Component baseFraction(int base) {
        if (base >= 100) return Component.translatable("guzhenren.display.base_full");
        Component tens = Component.translatable("guzhenren.display.base_tens." + (base / 10));
        int units = base % 10;
        return units == 0
                ? Component.translatable("guzhenren.display.base_round", tens)
                : Component.translatable("guzhenren.display.base_fraction", tens,
                Component.translatable("guzhenren.display.base_units." + units));
    }
}

package com.unknown.guzhenren.display;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

//  Every phrase that describes a cultivator, shared by the HUD and /gzr info so they can't drift.
//  Values only, never labels; each caller wraps in its own key.  CLAUDE.md "Color".
public final class ModDisplayText {

    private ModDisplayText() {}

    private static final String GAP = "  ";

    //  一转巅峰 / Rank I Peak. The separator lives in the lang key -- zh joins, en needs a space.
    public static MutableComponent realm(Aperture aperture) {
        return Component.translatable("guzhenren.display.realm",
                Component.translatable(aperture.rank().getTranslationKey()),
                Component.translatable(aperture.stage().getTranslationKey()));
    }

    //  甲等资质 [ 太日阳莽体 ] -- the bracket only shows up for a Ten-Extremes physique holder.
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

    //  本命·黑豕蛊 / Vital Black Boar Gu. On the NAME, so the one-gray-line tooltip rule keeps its
    //  single exception. ⚠ A block, not a one-liner: the key's name pushes it past 120.
    public static MutableComponent vital(Component name) {
        return Component.translatable("guzhenren.display.vital", name);
    }

    //  A path nobody may have chosen -- the primary before a Vital Gu, the secondary before he picks.
    //  ⚠ Reads [无] / [NONE] inline, the same shape an empty section header uses.
    public static MutableComponent path(@Nullable GuPath path) {
        return path == null
                ? Component.translatable("guzhenren.display.none")
                : Component.translatable(path.getTranslationKey());
    }

    //  86 [ 14岁 ]
    public static MutableComponent lifespan(BodyData body) {
        return Component.translatable("guzhenren.display.lifespan", body.lifespan(), body.age());
    }

    //  一转巅峰  甲等资质 [ 太日阳莽体 ] -- the HUD's first line.
    public static MutableComponent realmAndTalent(Aperture a) {return realm(a).append(GAP).append(talent(a));}

    //  800/800. A raw String, not a Component -- drawn straight into the HUD bar.
    public static String pool(long current, long max) {return current + "/" + max;}

    //  兽力虚影流's line: a bracket a family -- 一猪之力, 两猪之力, 百虎之力. An empty family never appears.
    //  ⚠ The reading is a CLOSED set -- 一两十百千万 are all defined, so a new beast brings only a name.
    public static MutableComponent beastStrengthLine(StrengthData data) {
        MutableComponent line = Component.empty();
        data.beastReadings().forEach((family, reading) -> line.append(beastReading(family, reading)));
        return line;
    }

    private static Component beastReading(MarkTag family, int reading) {
        return Component.translatable("guzhenren.display.strength.beast_reading",
                Component.translatable("guzhenren.display.strength.beast_number." + reading),
                Component.translatable("guzhenren.display.strength.beast." + family.getSerializedName()));
    }

    //  人力钧力流's one line: [9999斤] first, then a bracket a family -- 钧 (0..330) then 斤 (0..99), an
    //  empty one omitted. ⚠ The total is ARABIC and the readings are spelled; the two brackets sum to it.
    public static MutableComponent humanStrengthLine(StrengthData data) {
        MutableComponent line = Component.empty();
        int total = data.totalJin();
        if (total > 0) line.append(Component.translatable("guzhenren.display.strength.jin_total", total));
        appendFamily(line, "guzhenren.display.strength.jun_reading", data.junReading());
        appendFamily(line, "guzhenren.display.strength.jin_reading", data.jinReading());
        return line;
    }

    private static void appendFamily(MutableComponent line, String readingKey, int reading) {
        if (reading <= 0) return;
        line.append(Component.translatable(readingKey, strengthNumber(reading)));
    }

    //  A reading 1..330 spelled out: 五, 九十九, 一百零五, 一百一十, 三百三十.
    //  ⚠ 110 is 一百一十, NOT 一百十 -- once a hundred leads, the tens digit 1 needs its own word.
    private static Component strengthNumber(int n) {
        if (n < 100) return belowHundred(n, false);
        Component h = Component.translatable("guzhenren.display.strength.num_hundreds." + (n / 100));
        int rest = n % 100;
        if (rest == 0) return h;
        //  一百零五: a missing tens digit is spoken, or the units would read as tens.
        String join = rest < 10 ? "guzhenren.display.strength.num_join_zero" : "guzhenren.display.strength.num_join";
        return Component.translatable(join, h, belowHundred(rest, true));
    }

    //  1..99. `led` marks a hundreds digit before it, which is the one thing that changes 十 into 一十.
    private static Component belowHundred(int n, boolean led) {
        int tens = n / 10;
        int units = n % 10;
        Component t = tens == 0 ? null : Component.translatable(led && tens == 1
                ? "guzhenren.display.strength.num_tens_led.1"
                : "guzhenren.display.strength.num_tens." + tens);
        Component u = units > 0 ? Component.translatable("guzhenren.display.strength.num_units." + units) : null;
        if (t != null && u != null) return Component.translatable("guzhenren.display.strength.num_join", t, u);
        return t != null ? t : u;
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

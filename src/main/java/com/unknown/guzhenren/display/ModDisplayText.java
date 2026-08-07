package com.unknown.guzhenren.display;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Title;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public final class ModDisplayText {

    private ModDisplayText() {}

    private static final String GAP = "  ";

    public static MutableComponent realm(Aperture aperture) {
        return Component.translatable("guzhenren.display.realm",
                Component.translatable(aperture.rank().getTranslationKey()),
                Component.translatable(aperture.stage().getTranslationKey()));
    }

    public static MutableComponent realmTitle(Aperture aperture) {
        MutableComponent title = Component.translatable(Title.fromRank(aperture.rank()).getTranslationKey());
        if (aperture.rank() == Rank.NONE) return title;

        return Component.translatable("guzhenren.display.realm_title", realm(aperture), title);
    }

    public static MutableComponent talent(Aperture aperture) {
        MutableComponent line = Component.translatable(aperture.talent().getTranslationKey());
        if (aperture.extremePhysique() == ExtremePhysique.NONE) return line;

        return line.append(" ").append(Component.translatable("guzhenren.display.physique",
                Component.translatable(aperture.extremePhysique().getTranslationKey())));
    }

    public static MutableComponent guLine(Rank rank, GuPath path, String kindKey) {
        return Component.translatable("guzhenren.display.gu_line",
                Component.translatable(rank.getTranslationKey()),
                Component.translatable(path.getTranslationKey()),
                Component.translatable(kindKey));
    }

    public static MutableComponent wild(Component name) {return Component.translatable("guzhenren.display.wild", name);}

    public static MutableComponent vital(Component name) {
        return Component.translatable("guzhenren.display.vital", name);
    }

    public static MutableComponent path(@Nullable GuPath path) {
        return path == null
                ? Component.translatable("guzhenren.display.none")
                : Component.translatable(path.getTranslationKey());
    }

    public static MutableComponent lifespan(BodyData body) {
        return Component.translatable("guzhenren.display.lifespan", body.lifespan(), body.age());
    }

    public static MutableComponent realmAndTalent(Aperture a) {return realmTitle(a).append(GAP).append(talent(a));}

    public static String pool(long current, long max) {return current + "/" + max;}

    public static String attackBonus(double bonus) {return "+" + bonus;}

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

    public static MutableComponent humanStrengthLine(StrengthData data) {
        MutableComponent line = Component.empty();
        appendFamily(line, "guzhenren.display.strength.jun_reading", data.junReading());
        appendFamily(line, "guzhenren.display.strength.jin_reading", data.jinReading());
        return line;
    }

    public static MutableComponent strengthLabel(MutableComponent branchName, int totalJin) {
        if (totalJin <= 0) return branchName;
        return branchName.append(Component.translatable("guzhenren.display.strength.jin_total", totalJin));
    }

    private static void appendFamily(MutableComponent line, String readingKey, int reading) {
        if (reading <= 0) return;
        line.append(Component.translatable(readingKey, strengthNumber(reading)));
    }

    private static Component strengthNumber(int n) {
        if (n < 100) return belowHundred(n, false);
        Component h = Component.translatable("guzhenren.display.strength.num_hundreds." + (n / 100));
        int rest = n % 100;
        if (rest == 0) return h;
        String join = rest < 10 ? "guzhenren.display.strength.num_join_zero" : "guzhenren.display.strength.num_join";
        return Component.translatable(join, h, belowHundred(rest, true));
    }

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

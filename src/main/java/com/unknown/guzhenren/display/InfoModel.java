package com.unknown.guzhenren.display;

import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.data.body.BodyData;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.attachment.data.body.SoulData;
import com.unknown.guzhenren.attachment.data.body.StrengthData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.AttackService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.StaminaService;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.body.Race;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class InfoModel {

    private InfoModel() {}

    public static final int INDENT = 10;

    public record Row(int indent, Entry entry) {}

    public sealed interface Entry {}

    //region Aperture
    public record ApertureIndex(int number) implements Entry {}
    public record Realm(Aperture aperture) implements Entry {}
    public record Talent(Aperture aperture, boolean awakened) implements Entry {}
    public record Essence(Aperture aperture) implements Entry {}
    public record Distilled(Aperture aperture) implements Entry {}
    public record PathChoice(boolean primary, @Nullable GuPath path) implements Entry {}
    public record ApertureLife(ApertureState state) implements Entry {}
    //endregion

    //region Body
    public record BodyLife(LifeState state) implements Entry {}
    public record Form(LifeForm form) implements Entry {}
    public record RaceRow(Race race) implements Entry {}
    public record Soul(SoulData soul) implements Entry {}
    public record Stamina(long current, long max) implements Entry {}
    public record Lifespan(BodyData body) implements Entry {}
    public record PathsHeader(boolean empty) implements Entry {}
    public record PathRow(GuPath path, PathEntry entry) implements Entry {}
    public record QiHeader(GuAttainment attainment, long totalMark, long totalSpeck) implements Entry {}
    public record QiRow(QiKind kind, long amount) implements Entry {}
    public record StrengthHeader(boolean empty) implements Entry {}
    public record StrengthRow(StrengthBranch branch, int totalJin, Component reading) implements Entry {}
    public record CapacityRow(int usable, int total) implements Entry {}
    public record AttackRow(double bonus) implements Entry {}
    public record WisdomHeader(GuAttainment attainment) implements Entry {}
    //endregion

    //region Mind
    public record BrillianceRow(Brilliance brilliance) implements Entry {}
    public record MindHeader() implements Entry {}
    public record MindRow(WisdomType type, MindPool pool) implements Entry {}
    //endregion

    public static List<Row> aperture(Player player) {
        ApertureData data = ApertureService.get(player);
        List<Row> rows = new ArrayList<>();

        if (data.count() <= 1) {
            apertureBlock(rows, data.primary(), data.isAwakened(), 0);
            return rows;
        }
        for (int i = 0; i < data.count(); i++) {
            rows.add(new Row(0, new ApertureIndex(i + 1)));
            apertureBlock(rows, data.get(i), true, INDENT);
        }
        return rows;
    }

    private static void apertureBlock(List<Row> rows, Aperture aperture, boolean awakened, int indent) {
        rows.add(new Row(indent, new Realm(aperture)));
        rows.add(new Row(indent, new Talent(aperture, awakened)));
        if (awakened) {
            rows.add(new Row(indent, new Essence(aperture)));
            if (aperture.distilledEssence() > 0L) rows.add(new Row(indent, new Distilled(aperture)));
            rows.add(new Row(indent, new PathChoice(true, aperture.primaryPath())));
            rows.add(new Row(indent, new PathChoice(false, aperture.secondaryPath())));
        }
        if (!aperture.isAlive()) rows.add(new Row(indent, new ApertureLife(aperture.state())));
    }

    public static List<Row> body(Player player) {
        BodyData body = BodyService.get(player);
        SoulData soul = SoulService.get(player);
        StrengthData strength = StrengthService.get(player);
        List<Row> rows = new ArrayList<>();

        if (body.lifeState() != LifeState.ALIVE) rows.add(new Row(0, new BodyLife(body.lifeState())));
        rows.add(new Row(0, new Form(body.lifeForm())));
        rows.add(new Row(0, new RaceRow(body.race())));
        rows.add(new Row(0, new Soul(soul)));
        rows.add(new Row(0, new Stamina(StaminaService.current(player), StaminaService.max(player))));
        rows.add(new Row(0, new Lifespan(body)));
        if (strength.isEmpty()) return rows;

        if (strength.hasBranch(StrengthBranch.HUMAN)) {
            rows.add(new Row(0, new CapacityRow(StrengthService.usableJin(player), strength.totalJin())));
        }
        rows.add(new Row(0, new AttackRow(AttackService.bonus(player))));
        return rows;
    }

    public static List<Row> pathAchievement(Player player) {
        List<Row> rows = new ArrayList<>();
        strength(rows, player);
        wisdom(rows, player);
        qi(rows, player);
        paths(rows, player);
        return rows;
    }

    private static void paths(List<Row> rows, Player player) {
        List<Map.Entry<GuPath, PathEntry>> paths = PathService.visibleEntries(player).entrySet().stream()
                .filter(e -> e.getKey() != GuPath.QI).toList();

        rows.add(new Row(0, new PathsHeader(paths.isEmpty())));
        for (Map.Entry<GuPath, PathEntry> e : paths) {
            rows.add(new Row(INDENT, new PathRow(e.getKey(), e.getValue())));
        }
    }

    private static void qi(List<Row> rows, Player player) {
        rows.add(new Row(0, new QiHeader(PathService.attainment(player, GuPath.QI),
                PathService.mark(player, GuPath.QI), PathService.speck(player, GuPath.QI))));

        for (QiKind kind : QiKind.values()) {
            long amount = QiService.current(player, kind);
            if (amount > 0L) rows.add(new Row(INDENT, new QiRow(kind, amount)));
        }
    }

    private static void strength(List<Row> rows, Player player) {
        StrengthData data = StrengthService.get(player);
        rows.add(new Row(0, new StrengthHeader(data.isEmpty())));
        if (data.isEmpty()) return;

        if (data.hasBranch(StrengthBranch.BEASTS)) {
            rows.add(new Row(INDENT,
                    new StrengthRow(StrengthBranch.BEASTS, 0, ModDisplayText.beastStrengthLine(data))));
        }
        if (data.hasBranch(StrengthBranch.HUMAN)) {
            rows.add(new Row(INDENT, new StrengthRow(StrengthBranch.HUMAN, data.totalJin(),
                    ModDisplayText.humanStrengthLine(data))));
        }
    }

    private static void wisdom(List<Row> rows, Player player) {
        rows.add(new Row(0, new WisdomHeader(PathService.attainment(player, GuPath.WISDOM))));
    }

    public static List<Row> mind(Player player) {
        MindData mind = MindService.get(player);
        List<Row> rows = new ArrayList<>();

        rows.add(new Row(0, new BrillianceRow(mind.brilliance())));
        rows.add(new Row(0, new MindHeader()));
        for (WisdomType type : WisdomType.values()) {
            rows.add(new Row(0, new MindRow(type, mind.pool(type))));
        }
        return rows;
    }
}

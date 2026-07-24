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
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

//  WHICH rows an info view has, in what order, decided once and rendered twice: /gzr info and the G panel.
//  ⚠ Structure only, not text -- the command bakes label+indent into each key ('Essence:     %s / %s'),
//  the panel puts label and value in separate columns. Formatting stays per-surface.  CLAUDE.md "Info panel".
//  ⚠⚠ Entry is SEALED so both switches are exhaustive: a new row is a compile error until both surfaces
//  handle it. That guarantee replaced the "must not drift" comments the two used to carry.  CLAUDE.md.
public final class InfoModel {

    private InfoModel() {}

    //  Entries that sit under a header are indented by the panel. ⚠ The command ignores this: its own
    //  keys already begin with two spaces.
    public static final int INDENT = 10;

    public record Row(int indent, Entry entry) {}

    public sealed interface Entry {}

    //region Aperture
    public record ApertureIndex(int number) implements Entry {}
    public record Realm(Aperture aperture) implements Entry {}
    public record Talent(Aperture aperture, boolean awakened) implements Entry {}
    public record Essence(Aperture aperture) implements Entry {}
    //  ⚠ Only while the pool is non-empty -- outside a Liquor Worm [酒虫] it is always 0, and a row that
    //  reads 0 every day of a cultivator's life teaches nothing.
    public record Distilled(Aperture aperture) implements Entry {}
    //  ⚠ One record for both: only the label differs, and the panel hangs its picker off the secondary.
    public record PathChoice(boolean primary, @Nullable GuPath path) implements Entry {}
    public record ApertureLife(ApertureState state) implements Entry {}
    //endregion

    //region Body
    public record BodyLife(LifeState state) implements Entry {}
    public record Form(LifeForm form) implements Entry {}
    public record Soul(SoulData soul) implements Entry {}
    public record Lifespan(BodyData body) implements Entry {}
    public record PathsHeader(boolean empty) implements Entry {}
    public record PathRow(GuPath path, PathEntry entry) implements Entry {}
    public record QiHeader(GuAttainment attainment, long total) implements Entry {}
    public record QiRow(QiType type, long mark) implements Entry {}
    public record StrengthHeader(boolean empty) implements Entry {}
    public record StrengthRow(StrengthBranch branch, Component reading) implements Entry {}
    //endregion

    //region Mind
    public record BrillianceRow(Brilliance brilliance) implements Entry {}
    //  ⚠ Command only: on the panel the tab name is already this header.
    public record MindHeader() implements Entry {}
    public record MindRow(WisdomType type, MindPool pool) implements Entry {}
    //endregion

    //  A cultivator's apertures. ⚠ One aperture reads bare; two get numbered and indented, or the blocks
    //  read as one contradictory cultivator.
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

    //  ⚠ An unawakened cap is 0, and a 0/0 line only asks the player to keep looking at it -- so essence
    //  and both paths appear only once he has an aperture. Realm and aptitude still read mortal.
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
        List<Row> rows = new ArrayList<>();

        //  Alive is the norm and says nothing; zombified or dead is the line worth a row.
        if (body.lifeState() != LifeState.ALIVE) rows.add(new Row(0, new BodyLife(body.lifeState())));
        rows.add(new Row(0, new Form(body.lifeForm())));
        rows.add(new Row(0, new Soul(soul)));
        rows.add(new Row(0, new Lifespan(body)));

        paths(rows, player);
        qi(rows, player);
        strength(rows, player);
        return rows;
    }

    //  Every visible path except the Qi Path -- that one is its own section, so it is never listed twice.
    //  ⚠ An empty section reads on its own header, never as a separate line.
    private static void paths(List<Row> rows, Player player) {
        List<Map.Entry<GuPath, PathEntry>> paths = PathService.visibleEntries(player).entrySet().stream()
                .filter(e -> e.getKey() != GuPath.QI).toList();

        rows.add(new Row(0, new PathsHeader(paths.isEmpty())));
        for (Map.Entry<GuPath, PathEntry> e : paths) {
            rows.add(new Row(INDENT, new PathRow(e.getKey(), e.getValue())));
        }
    }

    //  Attainment plus total marks on the header, then only the types he actually has -- QiData is sparse.
    private static void qi(List<Row> rows, Player player) {
        rows.add(new Row(0, new QiHeader(PathService.attainment(player, GuPath.QI),
                PathService.mark(player, GuPath.QI))));

        for (QiType type : QiType.values()) {
            long mark = QiService.mark(player, type);
            if (mark > 0L) rows.add(new Row(INDENT, new QiRow(type, mark)));
        }
    }

    //  The Strength Path's branches: a beast-strengths row, and the Human Jun branch combined onto one.
    //  ⚠ The Strength Path also stays in the path list above -- that row is its specks, these are the
    //  grades those uses bought. Two different facts.
    private static void strength(List<Row> rows, Player player) {
        StrengthData data = StrengthService.get(player);
        rows.add(new Row(0, new StrengthHeader(data.isEmpty())));
        if (data.isEmpty()) return;

        if (data.hasBranch(StrengthBranch.BEASTS)) {
            rows.add(new Row(INDENT, new StrengthRow(StrengthBranch.BEASTS,
                    ModDisplayText.boarStrength(data.boarCount()))));
        }
        //  The whole Human Jun branch on ONE row -- the 钧 family then the 斤 family, empty ones omitted.
        if (data.hasBranch(StrengthBranch.HUMAN)) {
            rows.add(new Row(INDENT, new StrengthRow(StrengthBranch.HUMAN, ModDisplayText.humanStrengthLine(data))));
        }
    }

    //  All three cells, always -- MindData is dense, and a missing row would read as a bug.
    //  ⚠ The cells are NOT indented on the panel; the command's own key supplies its two spaces.
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

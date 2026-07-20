package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.custom.enums.strength.JunStrength;
import com.unknown.guzhenren.custom.enums.strength.StrengthBranch;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

//  /gzr info [aperture|body|mind] -- bare `info` is `info aperture` on yourself. Phrases: ModDisplayText.
//  ⚠ [targets] hangs off the sections, not bare `info` -- a bare word arg there would be ambiguous.
//  TODO(refactor): this row logic mirrors PlayerInfoScreen -- extract a shared InfoModel when the view
//  next grows. See CLAUDE.md "Info panel".
public final class CmdInfo {

    private CmdInfo() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("info")
                .executes(CmdInfo::aperture)
                .then(ModCommandSupport.withTargets(Commands.literal("aperture"), CmdInfo::aperture))
                .then(ModCommandSupport.withTargets(Commands.literal("body"), CmdInfo::body))
                .then(ModCommandSupport.withTargets(Commands.literal("mind"), CmdInfo::mind));
    }

    //  An unawakened player still gets the two lines -- they read mortal / unawakened, which is the answer.
    private static int aperture(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            ApertureData data = ApertureService.get(player);
            ModCommandFeedback.header(source);

            if (data.count() <= 1) {
                apertureLines(source, data.primary(), data.isAwakened());
                continue;
            }

            //  Two apertures: number them, or the blocks read as one contradictory cultivator.
            for (int i = 0; i < data.count(); i++) {
                ModCommandFeedback.detail(source, Component.translatable(
                        "guzhenren.command.info.aperture_index", i + 1));
                apertureLines(source, data.get(i), true);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void apertureLines(CommandSourceStack source, Aperture aperture, boolean awakened) {
        ModCommandFeedback.detail(source, Component.translatable(
                "guzhenren.command.info.realm", ModDisplayText.realm(aperture)));

        MutableComponent talent = ModDisplayText.talent(aperture);
        if (awakened) talent.append(muted(ModDisplayText.baseFraction(aperture.baseEssence())));
        ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.talent", talent));

        //  An unawakened cap is 0 -- a 0/0 line only asks the player to keep looking at it.
        if (awakened) {
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.essence",
                    aperture.currentEssence(), aperture.maxEssence()));
        }
        if (!aperture.isAlive()) {
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.aperture_state",
                    Component.translatable(aperture.state().getTranslationKey())));
        }
    }

    private static int body(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            BodyData body = BodyService.get(player);
            SoulData soul = SoulService.get(player);

            ModCommandFeedback.header(source);

            //  Alive is the norm and says nothing; zombie or dead is the line worth a row.
            if (body.lifeState() != LifeState.ALIVE) {
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.life_state",
                        Component.translatable(body.lifeState().getTranslationKey())));
            }

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.life_form",
                    Component.translatable(body.lifeForm().getTranslationKey())));

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.soul",
                            soul.currentSoul(), soul.maxSoul())
                    .append(muted(Component.translatable(soul.tier().getTranslationKey()))));

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.lifespan",
                    ModDisplayText.lifespan(body)));

            paths(source, player);
            qi(source, player);
            strength(source, player);
        }

        return Command.SINGLE_SUCCESS;
    }

    //  Path attainment: every visible path except the Qi Path -- that one is its own section, as in the panel:
    //  empty reads inline on the header, not a separate line.
    private static void paths(CommandSourceStack source, ServerPlayer player) {
        List<Map.Entry<GuPath, PathEntry>> paths = PathService.visibleEntries(player).entrySet().stream()
                .filter(e -> e.getKey() != GuPath.QI).toList();
        if (paths.isEmpty()) {
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.paths")
                    .append("  ").append(none()));
            return;
        }
        ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.paths"));
        for (Map.Entry<GuPath, PathEntry> e : paths) {
            ModCommandFeedback.detail(source, pathLine(e.getKey(), e.getValue()));
        }
    }

    //  Qi Path: attainment + total marks on the header (empty reads [NONE], total omitted while 0), then
    //  only the types he actually has -- QiData is sparse. Same shape as the panel's Qi Path section.
    private static void qi(CommandSourceStack source, ServerPlayer player) {
        GuAttainment attainment = PathService.attainment(player, GuPath.QI);
        Component value = attainment == GuAttainment.NONE ? none()
                : Component.translatable(attainment.getTranslationKey());
        MutableComponent header = Component.translatable("guzhenren.command.info.qi", value);
        long total = PathService.mark(player, GuPath.QI);
        if (total > 0L) header.append(Component.translatable("guzhenren.command.info.qi_total", total));
        ModCommandFeedback.detail(source, header);

        for (QiType type : QiType.values()) {
            long mark = QiService.mark(player, type);
            if (mark <= 0L) continue;
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.qi_entry",
                    Component.translatable(type.getTranslationKey()), mark));
        }
    }

    //  The Strength Path's two branches, one row each: how many beast strengths, and how many 斤 -- never
    //  which. Empty reads [NONE] inline, as the path list does.
    //  ⚠ 力道 also stays in 流派造诣 above -- that row is its specks, these are the grades they bought.
    private static void strength(CommandSourceStack source, ServerPlayer player) {
        StrengthData data = StrengthService.get(player);
        MutableComponent header = Component.translatable("guzhenren.command.info.strength");
        if (data.isEmpty()) {
            ModCommandFeedback.detail(source, header.append("  ").append(none()));
            return;
        }
        ModCommandFeedback.detail(source, header);
        if (data.hasBranch(StrengthBranch.BEASTS)) {
            ModCommandFeedback.detail(source, branchLine(StrengthBranch.BEASTS,
                    ModDisplayText.boarStrength(data.boarCount())));
        }
        //  One row per kind that has any. ⚠ A second kind would repeat the branch title -- revisit then.
        for (JunStrength kind : JunStrength.values()) {
            int count = data.junCount(kind);
            if (count <= 0) continue;
            ModCommandFeedback.detail(source, branchLine(StrengthBranch.HUMAN,
                    ModDisplayText.junStrength(kind, count)));
        }
    }

    private static Component branchLine(StrengthBranch branch, Component reading) {
        return Component.translatable("guzhenren.command.info.strength_entry",
                Component.translatable(branch.getTranslationKey()), reading);
    }

    //  All three cells, always -- MindData is dense, and a missing row would read as a bug.
    private static int mind(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            MindData mind = MindService.get(player);

            ModCommandFeedback.header(source);

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.brilliance",
                            Component.translatable(mind.brilliance().getTranslationKey()))
                    .append(muted(Component.translatable("guzhenren.command.info.brilliance_rate",
                            mind.brilliance().getThoughtsPerSecond()))));

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.mind"));

            for (WisdomType type : WisdomType.values()) {
                MindPool pool = mind.pool(type);
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.mind_entry",
                        Component.translatable(type.getTranslationKey()), pool.current(), pool.max()));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    //  Marks always; specks only when he has some -- most mortals sit at one denomination, not both.
    private static Component pathLine(GuPath path, PathEntry entry) {
        MutableComponent line = Component.translatable("guzhenren.command.info.path_entry",
                Component.translatable(path.getTranslationKey()),
                Component.translatable(entry.attainment().getTranslationKey()),
                entry.mark());
        if (entry.speck() > 0L) {
            line.append(Component.translatable("guzhenren.command.info.path_speck", entry.speck()));
        }
        return line;
    }

    private static Component none() {return Component.translatable("guzhenren.display.none");}

    //  " [八成九]" / " [一人魂]": derived detail for operators. The one place gray is not a feedback class.
    //  ⚠ Same [...] shape as the panel and chat -- do not reinvent it per surface.
    private static Component muted(Object value) {
        return Component.translatable("guzhenren.command.info.detail", value).withStyle(ChatFormatting.DARK_GRAY);
    }
}

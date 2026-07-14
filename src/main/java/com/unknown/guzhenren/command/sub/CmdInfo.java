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
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

//  /gzr info                    -- shorthand for `info aperture` on yourself
//  /gzr info aperture [targets] -- realm, aptitude, essence, and the aperture's own 生死
//  /gzr info body     [targets] -- 生死僵, 凡/仙, soul, lifespan, the 气 table, the path table
//  /gzr info mind     [targets] -- 才情 and the Mind Ocean
//  [targets] hangs off the sections, not bare `info` (else ambiguous). Phrases from ModDisplayText.
public final class CmdInfo {

    private CmdInfo() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("info")
                .executes(CmdInfo::aperture)
                .then(ModCommandSupport.withTargets(Commands.literal("aperture"), CmdInfo::aperture))
                .then(ModCommandSupport.withTargets(Commands.literal("body"), CmdInfo::body))
                .then(ModCommandSupport.withTargets(Commands.literal("mind"), CmdInfo::mind));
    }

    //  An unawakened player still gets the two lines -- they read 凡人 / 未觉醒, which is the answer.
    private static int aperture(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            ApertureData data = ApertureService.get(player);
            ModCommandFeedback.header(source);

            if (data.count() <= 1) {
                apertureLines(source, data.primary(), data.isAwakened());
                continue;
            }

            //  第二空窍: number them, or the two blocks read as one contradictory cultivator.
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

        ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.talent",
                ModDisplayText.talent(aperture).append(muted(aperture.baseEssence()))));

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

            //  生 is the norm and says nothing; 僵 / 死 is the line worth a row.
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

            qi(source, player);
            paths(source, player);
        }

        return Command.SINGLE_SUCCESS;
    }

    //  Only the 气 he actually has -- QiData is sparse, and a wall of zeroes says nothing.
    private static void qi(CommandSourceStack source, ServerPlayer player) {
        ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.qi"));

        long total = QiService.total(player);
        if (total <= 0L) {
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.qi_empty"));
            return;
        }

        for (QiType type : QiType.values()) {
            long mark = QiService.mark(player, type);
            if (mark <= 0L) continue;
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.qi_entry",
                    Component.translatable(type.getTranslationKey()), mark));
        }
    }

    //  visibleEntries, not the raw record: 气道's marks live in QiData and must be filled in.
    private static void paths(CommandSourceStack source, ServerPlayer player) {
        Map<GuPath, PathEntry> entries = PathService.visibleEntries(player);
        ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.paths"));

        if (entries.isEmpty()) {
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.path_empty"));
            return;
        }
        entries.forEach((path, entry) -> ModCommandFeedback.detail(source, pathLine(path, entry)));
    }

    //  All three cells, always -- MindData is dense, and a missing 情 row would read as a bug.
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

    private static Component pathLine(GuPath path, PathEntry entry) {
        return Component.translatable("guzhenren.command.info.path_entry",
                Component.translatable(path.getTranslationKey()),
                Component.translatable(entry.attainment().getTranslationKey()),
                entry.mark());
    }

    //  " (85)" / " (一人魂)": derived detail for operators. The one place gray is not a feedback class.
    private static Component muted(Object value) {
        return Component.translatable("guzhenren.command.info.detail", value).withStyle(ChatFormatting.DARK_GRAY);
    }
}

package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.data.body.PathEntry;
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.display.InfoModel;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

//  /gzr info [aperture|body|mind] -- bare `info` is `info aperture` on yourself. Phrases: ModDisplayText.
//  ⚠ [targets] hangs off the sections, not bare `info` -- a bare word arg there would be ambiguous.
//  ⚠ WHICH rows exist is InfoModel's; this file only turns each into a chat line. Every key bakes in its
//  own label and its own indent, which is why the panel cannot share them --  CLAUDE.md "Info panel".
public final class CmdInfo {

    private CmdInfo() {}

    private static final String PREFIX = "guzhenren.command.info.";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("info")
                .executes(context -> print(context, InfoModel::aperture))
                .then(ModCommandSupport.withTargets(Commands.literal("aperture"),
                        context -> print(context, InfoModel::aperture)))
                .then(ModCommandSupport.withTargets(Commands.literal("body"),
                        context -> print(context, InfoModel::body)))
                .then(ModCommandSupport.withTargets(Commands.literal("mind"),
                        context -> print(context, InfoModel::mind)));
    }

    //  One shape for all three sections: a tagged header per target, then a line per row.
    private static int print(CommandContext<CommandSourceStack> context,
                             Function<ServerPlayer, List<InfoModel.Row>> view) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            ModCommandFeedback.header(source);
            for (InfoModel.Row row : view.apply(player)) {
                ModCommandFeedback.detail(source, line(row.entry()));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    //  ⚠ Exhaustive over a sealed Entry: a new row cannot be added to InfoModel without this failing to
    //  compile. That is what replaced the old "must not drift" comment in two files.
    private static Component line(InfoModel.Entry entry) {
        return switch (entry) {
            case InfoModel.ApertureIndex e -> key("aperture_index", e.number());
            case InfoModel.Realm e -> key("realm", ModDisplayText.realm(e.aperture()));
            case InfoModel.Talent e -> talent(e);
            case InfoModel.Essence e -> key("essence", e.aperture().currentEssence(), e.aperture().maxEssence());
            case InfoModel.PathChoice e -> key(e.primary() ? "primary_path" : "secondary_path",
                    ModDisplayText.path(e.path()));
            case InfoModel.ApertureLife e -> key("aperture_state", enumName(e.state().getTranslationKey()));

            case InfoModel.BodyLife e -> key("life_state", enumName(e.state().getTranslationKey()));
            case InfoModel.Form e -> key("life_form", enumName(e.form().getTranslationKey()));
            case InfoModel.Soul e -> key("soul", e.soul().currentSoul(), e.soul().maxSoul())
                    .append(muted(enumName(e.soul().tier().getTranslationKey())));
            case InfoModel.Lifespan e -> key("lifespan", ModDisplayText.lifespan(e.body()));
            case InfoModel.PathsHeader e -> header("paths", e.empty());
            case InfoModel.PathRow e -> pathLine(e.path(), e.entry());
            case InfoModel.QiHeader e -> qiHeader(e);
            case InfoModel.QiRow e -> key("qi_entry", enumName(e.type().getTranslationKey()), e.mark());
            case InfoModel.StrengthHeader e -> header("strength", e.empty());
            case InfoModel.StrengthRow e -> key("strength_entry",
                    enumName(e.branch().getTranslationKey()), e.reading());

            case InfoModel.BrillianceRow e -> key("brilliance", enumName(e.brilliance().getTranslationKey()))
                    .append(muted(key("brilliance_rate", e.brilliance().getThoughtsPerSecond())));
            case InfoModel.MindHeader ignored -> key("mind");
            case InfoModel.MindRow e -> key("mind_entry", enumName(e.type().getTranslationKey()),
                    e.pool().current(), e.pool().max());
        };
    }

    private static MutableComponent talent(InfoModel.Talent e) {
        MutableComponent talent = ModDisplayText.talent(e.aperture());
        if (e.awakened()) talent.append(muted(ModDisplayText.baseFraction(e.aperture().baseEssence())));
        return key("talent", talent);
    }

    //  Attainment, or [NONE] while it is still none -- never a bare none. Total omitted while 0.
    private static MutableComponent qiHeader(InfoModel.QiHeader e) {
        Component value = e.attainment() == GuAttainment.NONE
                ? none()
                : enumName(e.attainment().getTranslationKey());
        MutableComponent header = key("qi", value);
        if (e.total() > 0L) header.append(key("qi_total", e.total()));
        return header;
    }

    //  Marks always; specks only when he has some -- most mortals sit at one denomination, not both.
    private static MutableComponent pathLine(GuPath path, PathEntry entry) {
        MutableComponent line = key("path_entry", enumName(path.getTranslationKey()),
                enumName(entry.attainment().getTranslationKey()), entry.mark());
        if (entry.speck() > 0L) line.append(key("path_speck", entry.speck()));
        return line;
    }

    //  ⚠ An empty section reads inline on its own header, never as a separate line.
    private static MutableComponent header(String id, boolean empty) {
        MutableComponent line = key(id);
        return empty ? line.append("  ").append(none()) : line;
    }

    private static MutableComponent key(String id, Object... args) {return Component.translatable(PREFIX + id, args);}
    private static MutableComponent enumName(String key) {return Component.translatable(key);}
    private static MutableComponent none() {return Component.translatable("guzhenren.display.none");}

    //  " [Eighty Nine]" / " [One-Person Soul]": derived detail for operators. The one place gray is not a
    //  feedback class. ⚠ Same [...] shape as the panel and chat -- do not reinvent it per surface.
    private static MutableComponent muted(Object value) {
        return Component.translatable(PREFIX + "detail", value).withStyle(ChatFormatting.DARK_GRAY);
    }
}

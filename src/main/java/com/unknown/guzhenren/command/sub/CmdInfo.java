package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.mind.MindData;
import com.unknown.guzhenren.attachment.data.mind.MindPool;
import com.unknown.guzhenren.attachment.data.path.PathEntry;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.MindService;
import com.unknown.guzhenren.attachment.service.PathService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import com.unknown.guzhenren.display.ModDisplayText;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

//  /gzr info                  -- shorthand for `info core` on yourself
//  /gzr info core   [targets] -- realm, aptitude, essence, soul, lifespan, life state
//  /gzr info path   [targets] -- the path table
//  /gzr info wisdom [targets] -- the Mind Ocean
//  [targets] hangs off the sections, not bare `info` (else ambiguous). Phrases from ModDisplayText.
public final class CmdInfo {

    private CmdInfo() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("info")
                .executes(CmdInfo::core)
                .then(ModCommandSupport.withTargets(Commands.literal("core"), CmdInfo::core))
                .then(ModCommandSupport.withTargets(Commands.literal("path"), CmdInfo::path))
                .then(ModCommandSupport.withTargets(Commands.literal("wisdom"), CmdInfo::wisdom));
    }

    private static int core(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            CoreData core = CoreService.get(player);
            SoulData soul = SoulService.get(player);

            ModCommandFeedback.header(source);

            ModCommandFeedback.detail(source, Component.translatable(
                    "guzhenren.command.info.realm", ModDisplayText.realm(core)));

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.talent",
                    ModDisplayText.talent(core).append(muted(core.baseEssence()))));

            if (core.isAwakened()) {
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.essence",
                        EssenceService.currentEssence(player), EssenceService.maxEssence(core)));
            }

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.soul",
                    soul.currentSoul(), soul.maxSoul())
                    .append(muted(Component.translatable(soul.tier().getTranslationKey()))));

            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.lifespan",
                    ModDisplayText.lifespan(LifespanService.get(player))));

            if (core.lifeState() != GuLifeState.ALIVE) {
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.life_state",
                        Component.translatable(core.lifeState().getTranslationKey())));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int path(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            Map<GuPath, PathEntry> entries = PathService.get(player).entries();

            ModCommandFeedback.header(source);
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.paths"));

            //  Sparse map -- an untouched cultivator has no rows, so say so rather than print an empty label.
            if (entries.isEmpty()) {
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.path_empty"));
            } else {
                entries.forEach((path, entry) -> ModCommandFeedback.detail(source, pathLine(path, entry)));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    //  All three cells, always -- MindData is dense, and a missing 情 row would read as a bug.
    private static int wisdom(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : ModCommandSupport.targets(context)) {
            MindData mind = MindService.get(player);

            ModCommandFeedback.header(source);
            ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.mind"));

            for (GuWisdomType type : GuWisdomType.values()) {
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

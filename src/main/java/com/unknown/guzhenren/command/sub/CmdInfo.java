package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.PathEntry;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.PathService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.util.ModDisplayText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

//  /gzr info
//
//    [GZR]
//    玩家修为：一转巅峰
//    玩家天赋：甲等资质 [ 太日阳莽体 ] (85)
//    玩家真元：800 / 800
//    玩家魂魄：100 / 100 (一人魂)
//    玩家寿元：86 [ 14岁 ]
//
//  The phrases come from ModDisplayText, so this reads identically to the HUD. Essence, life state and
//  paths print only when they say something: an unawakened mortal has no essence pool, an ALIVE player
//  is the default, and an empty path map is not a table.
public final class CmdInfo {

    private CmdInfo() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return ModCommandSupport.withTargets(Commands.literal("info"), CmdInfo::run);
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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

            var entries = PathService.get(player).entries();
            if (!entries.isEmpty()) {
                ModCommandFeedback.detail(source, Component.translatable("guzhenren.command.info.paths"));
                entries.forEach((path, entry) -> ModCommandFeedback.detail(source, pathLine(path, entry)));
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

    //  The trailing " (85)" / " (一人魂)": derived detail an operator needs and a player does not.
    //  Dimmed so it stays out of the way -- the one place gray is not a feedback class.
    private static Component muted(Object value) {
        return Component.translatable("guzhenren.command.info.detail", value).withStyle(ChatFormatting.DARK_GRAY);
    }
}

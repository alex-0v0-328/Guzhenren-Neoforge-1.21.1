package com.unknown.guzhenren.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.command.sub.CmdInfo;
import com.unknown.guzhenren.command.sub.CmdReset;
import com.unknown.guzhenren.command.sub.aperture.CmdAperture;
import com.unknown.guzhenren.command.sub.aperture.CmdAwaken;
import com.unknown.guzhenren.command.sub.body.CmdBody;
import com.unknown.guzhenren.command.sub.mind.CmdMind;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

//  /guzhenren (alias /gzr) root: one .then() per domain -- 空窍 / 肉身 / 脑海, plus info and reset.
//  A sibling mod adds its own root, never a branch here. See CLAUDE.md "Commands".
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class ModCommand {

    private ModCommand() {}

    private static final int PERMISSION_LEVEL = 2;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(
                Commands.literal("guzhenren")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .then(CmdInfo.node())
                        .then(CmdAwaken.node())
                        .then(CmdReset.node())
                        .then(CmdAperture.node())
                        .then(CmdBody.node())
                        .then(CmdMind.node()));

        dispatcher.register(Commands.literal("gzr")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .redirect(root));
    }
}

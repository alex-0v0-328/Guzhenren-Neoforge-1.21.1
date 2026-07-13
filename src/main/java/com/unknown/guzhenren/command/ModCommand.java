package com.unknown.guzhenren.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.command.sub.CmdAwaken;
import com.unknown.guzhenren.command.sub.CmdCore;
import com.unknown.guzhenren.command.sub.CmdEssence;
import com.unknown.guzhenren.command.sub.CmdInfo;
import com.unknown.guzhenren.command.sub.CmdLifeState;
import com.unknown.guzhenren.command.sub.CmdLifespan;
import com.unknown.guzhenren.command.sub.CmdPath;
import com.unknown.guzhenren.command.sub.CmdPhysique;
import com.unknown.guzhenren.command.sub.CmdSoul;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

//  /guzhenren, alias /gzr. The root and nothing else: one branch per system, each in command/sub.
//
//  Full grammar and the awakening gate: CLAUDE.md "Commands". The machinery every branch shares
//  (optional [targets], the gate, per-target apply): ModCommandSupport.
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
                        .then(CmdAwaken.resetNode())
                        .then(CmdAwaken.awakenNode())
                        .then(CmdLifeState.node())
                        .then(CmdCore.node())
                        .then(CmdPhysique.node())
                        .then(CmdEssence.node())
                        .then(CmdSoul.node())
                        .then(CmdLifespan.node())
                        .then(CmdPath.node()));

        dispatcher.register(Commands.literal("gzr")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .redirect(root));
    }
}

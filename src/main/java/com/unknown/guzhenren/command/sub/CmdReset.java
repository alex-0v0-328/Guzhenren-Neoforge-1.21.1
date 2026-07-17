package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr reset -- back to a brand-new mortal. A reset is a rebirth: it re-rolls Brilliance (see onBirth).
public final class CmdReset {

    private CmdReset() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return ModCommandSupport.withTargets(Commands.literal("reset"),
                context -> ModCommandSupport.apply(context, CmdReset::reset));
    }

    private static void reset(ServerPlayer player) {
        PlayerDataService.resetAll(player);
        ModCommandSupport.refreshCommands(player);
    }
}

package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

//  /gzr awaken  -- 开窍, refuses if the aperture is already open.
//  /gzr reset   -- back to a brand-new mortal.
//  Guarded, not hidden; re-rolling is reset + awaken. See CLAUDE.md "The awakening gate".
public final class CmdAwaken {

    private CmdAwaken() {}

    public static ArgumentBuilder<CommandSourceStack, ?> awakenNode() {
        return ModCommandSupport.withTargets(Commands.literal("awaken"),
                context -> ModCommandSupport.applyIf(context, ModCommandSupport.AWAKENED.negate(),
                        ModCommandSupport.FAILED_AWAKENED, CmdAwaken::awaken));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> resetNode() {
        return ModCommandSupport.withTargets(Commands.literal("reset"),
                context -> ModCommandSupport.apply(context, CmdAwaken::reset));
    }

    //  开窍 only -- 才情 is dealt at birth and awaken must not touch it. See CLAUDE.md "Birth".
    private static void awaken(ServerPlayer player) {
        CoreService.awaken(player);
        refreshCommands(player);
    }

    private static void reset(ServerPlayer player) {
        PlayerDataService.resetAll(player);
        refreshCommands(player);
    }

    //  awaken/reset flip the requires() gate, so the tree must be resent or the branches lag a relog.
    private static void refreshCommands(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) server.getCommands().sendCommands(player);
    }
}

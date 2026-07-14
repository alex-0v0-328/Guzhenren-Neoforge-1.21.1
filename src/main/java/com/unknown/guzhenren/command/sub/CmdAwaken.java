package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

//  /gzr awaken  -- 开窍, the only leaf that refuses on the *presence* of an aperture.
//  /gzr reset   -- back to a brand-new mortal.
//
//  Guarded, not hidden: hiding awaken would take `/gzr awaken Bob` away from an operator who happens
//  to be awakened themselves. Re-rolling is reset + awaken. See CLAUDE.md "The awakening gate".
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

    private static void awaken(ServerPlayer player) {
        CoreService.awaken(player);
        refreshCommands(player);
    }

    private static void reset(ServerPlayer player) {
        PlayerDataService.resetAll(player);
        refreshCommands(player);
    }

    //  The awakening gate is a requires() on the tree, resolved when the tree is sent to the client.
    //  awaken/reset flip that answer, so without a resend the cultivation branches never appear (or
    //  never disappear) until the next relog. sendCommands rebuilds the tree for this one player.
    private static void refreshCommands(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) server.getCommands().sendCommands(player);
    }
}

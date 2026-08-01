package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class CmdAwaken {

    private CmdAwaken() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return ModCommandSupport.withTargets(Commands.literal("awaken"),
                context -> ModCommandSupport.applyIf(context, ModCommandSupport.AWAKENED.negate(),
                        ModCommandSupport.FAILED_AWAKENED, CmdAwaken::awaken));
    }

    private static void awaken(ServerPlayer player) {
        ApertureService.awaken(player);
        ModCommandSupport.refreshCommands(player);
    }
}

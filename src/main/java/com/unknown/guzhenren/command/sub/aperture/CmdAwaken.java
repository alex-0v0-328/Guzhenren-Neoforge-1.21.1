package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr awaken -- awakening (开窍), the one-way transition into having an aperture at all.
//  ⚠ At the root, not under `aperture`: hoisting it is what lets that branch carry a single requires().
public final class CmdAwaken {

    private CmdAwaken() {}

    //  Refuses a holder who already has one -- re-rolling is reset + awaken, never a second meaning for
    //  awaken. Guarded, not hidden: an operator must be able to run it on someone else.
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

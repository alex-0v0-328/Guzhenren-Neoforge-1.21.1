package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr awaken -- 开窍, the one-way transition into having an aperture at all.
//  At the root, not under `aperture`: it is the only leaf there that an unawakened player may run, and
//  hoisting it lets the whole /gzr aperture branch carry a single requires(). See CLAUDE.md.
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

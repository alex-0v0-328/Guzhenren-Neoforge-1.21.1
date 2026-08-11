package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /gzr awaken}: opens an aperture [空窍] without the Gu that normally does it.
 *
 * <p>⚠ The service does not refuse a holder who is already awakened; it appends another one. The gate
 * has to live here, in the caller.
 *
 * @author Alex
 * @since 1.0.0
 */
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

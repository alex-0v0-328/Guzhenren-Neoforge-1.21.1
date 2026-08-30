package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.PlayerDataService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /gzr reset}: puts a player back to what they were born as.
 *
 * <p>Delegates to {@link com.unknown.guzhenren.attachment.PlayerDataService#resetAll} and then calls
 * {@link com.unknown.guzhenren.command.ModCommandSupport#refreshCommands}, because the reset flips the
 * awakened gate and the client must see the updated tree.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.command.sub.aperture.CmdAwaken
 * @since 1.0.0
 */

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

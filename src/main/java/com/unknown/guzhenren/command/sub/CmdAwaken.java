package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.attachment.service.PlayerDataService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr awaken   -- 开窍, and the only leaf that refuses on the *presence* of an aperture.
//  /gzr reset    -- back to a brand-new mortal.
//
//  awaken is a one-way transition, not a setter, so it is guarded rather than hidden: hiding it would
//  take `/gzr awaken Bob` away from an operator who happens to be awakened themselves. To re-roll, say
//  so -- reset, then awaken. See CLAUDE.md "The awakening gate".
public final class CmdAwaken {

    private CmdAwaken() {}

    public static ArgumentBuilder<CommandSourceStack, ?> awakenNode() {
        return ModCommandSupport.withTargets(Commands.literal("awaken"),
                context -> ModCommandSupport.applyIf(context, ModCommandSupport.AWAKENED.negate(),
                        ModCommandSupport.FAILED_AWAKENED, CoreService::awaken));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> resetNode() {
        return ModCommandSupport.withTargets(Commands.literal("reset"),
                context -> ModCommandSupport.apply(context, PlayerDataService::resetAll));
    }
}

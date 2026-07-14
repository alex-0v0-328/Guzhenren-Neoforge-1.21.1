package com.unknown.guzhenren.command.sub.mind;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.wisdom.Brilliance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr mind brilliance set <v> | up | down
//  /gzr mind wisdom ...   (CmdWisdom)
//  Ungated: 念 is thought, not cultivation -- a mortal has a 脑海 too.
public final class CmdMind {

    private CmdMind() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("mind")
                .then(brilliance())
                .then(CmdWisdom.node());
    }

    //  才情 is 念's regen rate -- a graded enum, so set / up / down. See CLAUDE.md "Commands".
    private static ArgumentBuilder<CommandSourceStack, ?> brilliance() {
        return ModCommandSupport.enumSetNode("brilliance", Brilliance.values(), MindService::setBrilliance,
                        ModCommandSupport.ANYONE, null)
                .then(shift("up", 1))
                .then(shift("down", -1));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> shift(String literal, int delta) {
        return ModCommandSupport.withTargets(Commands.literal(literal),
                context -> ModCommandSupport.apply(context, player -> MindService.shiftBrilliance(player, delta)));
    }
}

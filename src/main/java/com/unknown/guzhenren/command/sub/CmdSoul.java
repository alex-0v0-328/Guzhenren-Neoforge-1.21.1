package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr soul max|current set|add <long> | refill
//
//  Ungated: an ordinary mortal has 100/100 soul whether an aperture was ever opened.
public final class CmdSoul {

    private CmdSoul() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("soul")
                .then(Commands.literal("max")
                        .then(ModCommandSupport.longNode("set", SoulService::setMax))
                        .then(ModCommandSupport.longNode("add", SoulService::addMax)))
                .then(Commands.literal("current")
                        .then(ModCommandSupport.longNode("set", SoulService::setCurrent))
                        .then(ModCommandSupport.longNode("add", SoulService::addCurrent)))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.apply(context, SoulService::refill)));
    }
}

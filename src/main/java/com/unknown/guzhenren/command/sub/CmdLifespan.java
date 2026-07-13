package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.command.ModCommandSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr lifespan lifespan|age set|add <long>
//
//  Ungated: an unawakened mortal still ages, and still dies of old age.
public final class CmdLifespan {

    private CmdLifespan() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("lifespan")
                .then(Commands.literal("lifespan")
                        .then(ModCommandSupport.longNode("set", LifespanService::setLifespan))
                        .then(ModCommandSupport.longNode("add", LifespanService::addLifespan)))
                .then(Commands.literal("age")
                        .then(ModCommandSupport.longNode("set", LifespanService::setAge))
                        .then(ModCommandSupport.longNode("add", LifespanService::addAge)));
    }
}

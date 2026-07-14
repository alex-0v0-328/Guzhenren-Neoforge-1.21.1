package com.unknown.guzhenren.command.sub.mind;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr mind wisdom thoughts|wills|emotions current|max set|add|sub <long> | refill
//  ⚠ One literal per cell, not an enum arg: a word arg parses any word, so Brigadier would call it
//  ambiguous with any literal sibling. Do NOT tidy this back into a single <type> argument.
//  refill fills to the cap, never over -- an operator tool, not a game mechanic. See MindService.
public final class CmdWisdom {

    private CmdWisdom() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        LiteralArgumentBuilder<CommandSourceStack> wisdom = Commands.literal("wisdom");
        for (WisdomType type : WisdomType.values()) {
            wisdom.then(cell(type));
        }
        return wisdom;
    }

    //  The type is closed over, so every leaf is a plain long node -- no re-read off the context.
    private static ArgumentBuilder<CommandSourceStack, ?> cell(WisdomType type) {
        return Commands.literal(type.getSerializedName())
                .then(Commands.literal("current")
                        .then(ModCommandSupport.longNode("set", (p, v) -> MindService.setCurrent(p, type, v)))
                        .then(ModCommandSupport.longNode("add", (p, v) -> MindService.addCurrent(p, type, v)))
                        .then(ModCommandSupport.longNode("sub", (p, v) -> MindService.addCurrent(p, type, -v))))
                .then(Commands.literal("max")
                        .then(ModCommandSupport.longNode("set", (p, v) -> MindService.setMax(p, type, v)))
                        .then(ModCommandSupport.longNode("add", (p, v) -> MindService.addMax(p, type, v)))
                        .then(ModCommandSupport.longNode("sub", (p, v) -> MindService.addMax(p, type, -v))))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.apply(context, player -> MindService.refill(player, type))));
    }
}

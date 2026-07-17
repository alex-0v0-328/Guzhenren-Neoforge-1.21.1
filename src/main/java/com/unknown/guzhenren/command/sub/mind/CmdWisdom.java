package com.unknown.guzhenren.command.sub.mind;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.unknown.guzhenren.attachment.service.mind.MindService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.wisdom.WisdomType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr mind wisdom thoughts|wills|emotions current|max set|add|sub <long> | refill (to the cap, never over).
//  ⚠ One literal per cell, never an enum arg -- a word arg is ambiguous with any literal sibling.
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

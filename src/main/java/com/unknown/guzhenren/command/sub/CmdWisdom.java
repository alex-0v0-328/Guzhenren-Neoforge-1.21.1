package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.unknown.guzhenren.attachment.service.MindService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.wisdom.GuBrilliance;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr wisdom thoughts|wills|emotions current|max set|add|sub <long> | refill
//  /gzr wisdom brilliance set <v> | up | down
//  ⚠ One literal per cell, not an enum arg: a word arg is ambiguous with the `brilliance` sibling.
//  Ungated: a mortal has a 脑海 too. refill fills to the cap (never over) -- see MindService.
public final class CmdWisdom {

    private CmdWisdom() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        LiteralArgumentBuilder<CommandSourceStack> wisdom = Commands.literal("wisdom");
        for (GuWisdomType type : GuWisdomType.values()) {
            wisdom.then(cell(type));
        }
        return wisdom.then(brilliance());
    }

    //  The type is closed over, so every leaf is a plain long node -- no re-read off the context.
    private static ArgumentBuilder<CommandSourceStack, ?> cell(GuWisdomType type) {
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

    //  才情 is 念's regen rate -- a graded enum, so set / up / down. See CLAUDE.md "Commands".
    private static ArgumentBuilder<CommandSourceStack, ?> brilliance() {
        return ModCommandSupport.enumSetNode("brilliance", GuBrilliance.values(), MindService::setBrilliance,
                        ModCommandSupport.ANYONE, null)
                .then(shift("up", 1))
                .then(shift("down", -1));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> shift(String literal, int delta) {
        return ModCommandSupport.withTargets(Commands.literal(literal),
                context -> ModCommandSupport.apply(context, player -> MindService.shiftBrilliance(player, delta)));
    }
}

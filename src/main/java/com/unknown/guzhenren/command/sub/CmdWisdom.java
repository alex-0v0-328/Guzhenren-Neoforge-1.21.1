package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.MindService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.wisdom.GuWisdomType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr wisdom <type> current|max set|add|sub <long> | refill
//  Ungated: a mortal has a 脑海 too. refill fills to the cap (never over) -- see MindService.
public final class CmdWisdom {

    private CmdWisdom() {}

    private static final String ARG_TYPE = "type";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("wisdom")
                .then(ModEnumArgument.arg(ARG_TYPE, GuWisdomType.values())
                        .then(Commands.literal("current")
                                .then(poolNode("set", MindService::setCurrent))
                                .then(poolNode("add", MindService::addCurrent))
                                .then(poolNode("sub", (p, t, v) -> MindService.addCurrent(p, t, -v))))
                        .then(Commands.literal("max")
                                .then(poolNode("set", MindService::setMax))
                                .then(poolNode("add", MindService::addMax))
                                .then(poolNode("sub", (p, t, v) -> MindService.addMax(p, t, -v))))
                        .then(refill()));
    }

    //  Every leaf re-reads the type off the node above, so none fit the plain builders -- like CmdPath.
    private static ArgumentBuilder<CommandSourceStack, ?> poolNode(String literal, PoolOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuWisdomType type = ModEnumArgument.get(context, ARG_TYPE, GuWisdomType.values());
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, type, value));
                }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> refill() {
        return ModCommandSupport.withTargets(Commands.literal("refill"), context -> {
            GuWisdomType type = ModEnumArgument.get(context, ARG_TYPE, GuWisdomType.values());
            return ModCommandSupport.apply(context, player -> MindService.refill(player, type));
        });
    }

    @FunctionalInterface
    private interface PoolOperation {
        void apply(ServerPlayer player, GuWisdomType type, long value);
    }
}

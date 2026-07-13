package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.PathService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr path <path> mark       set|add|sub <long> | up | down
//  /gzr path <path> attainment set <v> | add|sub <tiers> | up | down
//
//  道痕 is a raw count, so add/sub and up/down (+-1) all deal in points. 造诣 is graded, so add/sub
//  count *tiers* and up/down is one tier. The two never move each other -- see PathService.
//
//  Every leaf has to re-read the path off the node above it, which is why none of them can use the
//  plain builders in ModCommandSupport.
public final class CmdPath {

    private CmdPath() {}

    private static final String ARG_PATH = "path";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("path")
                .then(ModEnumArgument.arg(ARG_PATH, GuPath.values())
                        .then(mark())
                        .then(attainment()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> mark() {
        return Commands.literal("mark")
                .then(markNode("set", PathService::setMark))
                .then(markNode("add", PathService::addMark))
                .then(markNode("sub", (player, path, value) -> PathService.addMark(player, path, -value)))
                .then(markShift("up", 1L))
                .then(markShift("down", -1L));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> attainment() {
        return Commands.literal("attainment")
                .then(Commands.literal("set")
                        .then(ModCommandSupport.withTargets(
                                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, GuPathAttainment.values()),
                                context -> {
                                    GuPath path = pathOf(context);
                                    GuPathAttainment value = ModEnumArgument.get(
                                            context, ModCommandSupport.ARG_VALUE, GuPathAttainment.values());
                                    return ModCommandSupport.apply(context,
                                            player -> PathService.setAttainment(player, path, value));
                                })))
                .then(attainmentNode("add", 1))
                .then(attainmentNode("sub", -1))
                .then(attainmentShift("up", 1))
                .then(attainmentShift("down", -1));
    }

    //region builders

    private static ArgumentBuilder<CommandSourceStack, ?> markNode(String literal, MarkOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuPath path = pathOf(context);
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, path, value));
                }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> markShift(String literal, long delta) {
        return ModCommandSupport.withTargets(Commands.literal(literal), context -> {
            GuPath path = pathOf(context);
            return ModCommandSupport.apply(context, player -> PathService.addMark(player, path, delta));
        });
    }

    //  The sign says which way, the argument says how far.
    private static ArgumentBuilder<CommandSourceStack, ?> attainmentNode(String literal, int sign) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE,
                        IntegerArgumentType.integer(0, GuPathAttainment.values().length - 1)),
                context -> {
                    GuPath path = pathOf(context);
                    int tiers = IntegerArgumentType.getInteger(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context,
                            player -> PathService.shiftAttainment(player, path, sign * tiers));
                }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> attainmentShift(String literal, int delta) {
        return ModCommandSupport.withTargets(Commands.literal(literal), context -> {
            GuPath path = pathOf(context);
            return ModCommandSupport.apply(context, player -> PathService.shiftAttainment(player, path, delta));
        });
    }

    private static GuPath pathOf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ModEnumArgument.get(context, ARG_PATH, GuPath.values());
    }

    //endregion

    @FunctionalInterface
    private interface MarkOperation {
        void apply(ServerPlayer player, GuPath path, long value);
    }
}

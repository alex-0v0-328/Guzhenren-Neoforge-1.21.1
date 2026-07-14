package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.command.ModCommandFeedback;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

//  /gzr body path <path> mark       set|add|sub <long>
//  /gzr body path <path> attainment set <v> | up | down
//  道痕 a raw count, 造诣 graded; every leaf re-reads the path, so none fit the plain builders.
//  ⚠ 气道's marks are read-only here -- they are QiData's total. Its attainment is settable as usual.
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
                .then(markNode("sub", (player, path, value) -> PathService.addMark(player, path, -value)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> attainment() {
        return Commands.literal("attainment")
                .then(Commands.literal("set")
                        .then(ModCommandSupport.withTargets(
                                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, GuAttainment.values()),
                                context -> {
                                    GuPath path = pathOf(context);
                                    GuAttainment value = ModEnumArgument.get(
                                            context, ModCommandSupport.ARG_VALUE, GuAttainment.values());
                                    return ModCommandSupport.apply(context,
                                            player -> PathService.setAttainment(player, path, value));
                                })))
                .then(attainmentShift("up", 1))
                .then(attainmentShift("down", -1));
    }

    //region builders

    private static ArgumentBuilder<CommandSourceStack, ?> markNode(String literal, MarkOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuPath path = pathOf(context);
                    if (path == GuPath.QI) return refuseQi(context);

                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, path, value));
                }));
    }

    //  Not a per-target refusal: the argument itself is what is wrong, whoever the targets are.
    private static int refuseQi(CommandContext<CommandSourceStack> context) {
        ModCommandFeedback.failure(context.getSource(),
                Component.translatable(ModCommandSupport.FAILED_QI_MARK));
        return 0;
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

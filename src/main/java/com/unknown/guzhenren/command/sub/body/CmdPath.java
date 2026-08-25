package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.body.PathService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.path.GuAttainment;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /gzr body path}: writes attainment [造诣] and marks [道痕].
 *
 * <p>Uses {@link com.unknown.guzhenren.command.ModEnumArgument} for the path argument, then offers
 * {@code set}/{@code add}/{@code sub} for marks and {@code set}/{@code up}/{@code down} for
 * attainment. All writes delegate to {@link com.unknown.guzhenren.attachment.service.body.PathService}.
 *
 * <p>☠ A command books 自然 [NATURAL] and can name no other tag. A handwritten source tag cannot be
 * told from what a Gu laid down, and that is how a race mark was forged onto a path no race revokes.
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.command.ModEnumArgument
 * @since 1.0.0
 */

public final class CmdPath {

    private CmdPath() {}

    private static final String ARG_PATH = "path";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("path")
                .then(ModEnumArgument.arg(ARG_PATH, GuPath.values())
                        .then(tally("mark", PathService::setMark, PathService::addMark))
                        .then(attainment()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> tally(
            String literal, TallyOperation set, TallyOperation add) {
        return Commands.literal(literal)
                .then(countNode("set", set))
                .then(countNode("add", add))
                .then(countNode("sub", (player, path, tag, value) -> add.apply(player, path, tag, -value)));
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

    private static ArgumentBuilder<CommandSourceStack, ?> countNode(String literal, TallyOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuPath path = pathOf(context);
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context,
                            player -> operation.apply(player, path, MarkTag.NATURAL, value));
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
    private interface TallyOperation {
        void apply(ServerPlayer player, GuPath path, MarkTag tag, long value);
    }
}

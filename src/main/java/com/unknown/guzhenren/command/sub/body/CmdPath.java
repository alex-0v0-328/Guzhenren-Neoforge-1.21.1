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
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /gzr path}: reads and writes attainment [造诣], marks [道痕] and specks [碎屑].
 *
 * <p>⚠ Every write names a tag, and the record drops a tag that belongs to another path. A write with
 * the wrong tag reports success and changes nothing.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class CmdPath {

    private CmdPath() {}

    private static final String ARG_PATH = "path";
    private static final String ARG_TAG = "tag";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("path")
                .then(ModEnumArgument.arg(ARG_PATH, GuPath.values())
                        .then(tagged("mark", PathService::setMark, PathService::addMark))
                        .then(tagged("speck", PathService::setSpeck, PathService::addSpeck))
                        .then(attainment()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> tagged(
            String literal, TagOperation set, TagOperation add) {
        return Commands.literal(literal).then(ModEnumArgument.arg(ARG_TAG, MarkTag.values())
                .then(countNode("set", set))
                .then(countNode("add", add))
                .then(countNode("sub", (player, path, tag, value) -> add.apply(player, path, tag, -value))));
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

    private static ArgumentBuilder<CommandSourceStack, ?> countNode(String literal, TagOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuPath path = pathOf(context);
                    MarkTag tag = tagOf(context);
                    if (!tag.fitsOn(path)) return refuseTag(context, tag, path);

                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context,
                            player -> operation.apply(player, path, tag, value));
                }));
    }

    private static int refuseTag(CommandContext<CommandSourceStack> context, MarkTag tag, GuPath path) {
        ModCommandFeedback.failure(context.getSource(), Component.translatable(
                ModCommandSupport.FAILED_TAG_PATH,
                Component.translatable(tag.getTranslationKey()),
                Component.translatable(path.getTranslationKey())));
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

    private static MarkTag tagOf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ModEnumArgument.get(context, ARG_TAG, MarkTag.values());
    }

    //endregion

    @FunctionalInterface
    private interface TagOperation {
        void apply(ServerPlayer player, GuPath path, MarkTag tag, long value);
    }
}

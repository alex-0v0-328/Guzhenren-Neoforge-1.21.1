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
import org.jetbrains.annotations.Nullable;

//  /gzr body path <p> -- marks and specks are raw counts, attainment is graded; every leaf re-reads the path.
//  ⚠ A featured path's mark/speck are read-only here (its sub-system's total); attainment is not.
public final class CmdPath {

    private CmdPath() {}

    private static final String ARG_PATH = "path";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("path")
                .then(ModEnumArgument.arg(ARG_PATH, GuPath.values())
                        .then(mark())
                        .then(speck())
                        .then(attainment()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> mark() {
        return Commands.literal("mark")
                .then(countNode("set", PathService::setMark, ModCommandSupport.FAILED_QI_MARK))
                .then(countNode("add", PathService::addMark, ModCommandSupport.FAILED_QI_MARK))
                .then(countNode("sub", (p, path, v) -> PathService.addMark(p, path, -v),
                        ModCommandSupport.FAILED_QI_MARK));
    }

    //  ⚠ No featured refusal here: specks are ordinary on every path, the Qi Path included.
    private static ArgumentBuilder<CommandSourceStack, ?> speck() {
        return Commands.literal("speck")
                .then(countNode("set", PathService::setSpeck, null))
                .then(countNode("add", PathService::addSpeck, null))
                .then(countNode("sub", (p, path, v) -> PathService.addSpeck(p, path, -v), null));
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

    //  A null key means this leaf has nothing to refuse -- specks write on every path.
    private static ArgumentBuilder<CommandSourceStack, ?> countNode(
            String literal, CountOperation operation, @Nullable String featuredRefusalKey) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    GuPath path = pathOf(context);
                    if (featuredRefusalKey != null && path.isFeatured()) {
                        return refuse(context, featuredRefusalKey);
                    }

                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, path, value));
                }));
    }

    //  Not a per-target refusal: the argument itself is what is wrong, whoever the targets are.
    private static int refuse(CommandContext<CommandSourceStack> context, String key) {
        ModCommandFeedback.failure(context.getSource(), Component.translatable(key));
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
    private interface CountOperation {
        void apply(ServerPlayer player, GuPath path, long value);
    }
}

package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.qi.QiKind;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /gzr qi}: reads and writes Qi [气] holdings.
 *
 * <p>⚠ A holding is a time anchor, so an amount written here begins decaying at once. Reading it back
 * a moment later and finding it smaller is correct behavior.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class CmdQi {

    private CmdQi() {}

    private static final String ARG_KIND = "kind";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("qi").then(ModEnumArgument.arg(ARG_KIND, QiKind.values())
                .then(countNode("set", QiService::set))
                .then(countNode("add", QiService::add))
                .then(countNode("sub", (player, kind, value) -> QiService.add(player, kind, -value))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> countNode(String literal, QiOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    QiKind kind = kindOf(context);
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, kind, value));
                }));
    }

    private static QiKind kindOf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ModEnumArgument.get(context, ARG_KIND, QiKind.values());
    }

    @FunctionalInterface
    private interface QiOperation {
        void apply(ServerPlayer player, QiKind kind, long value);
    }
}

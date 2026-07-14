package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.body.QiService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.qi.QiType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr body qi <type> set|add|sub <long>
//  A raw count with no cap and no second field, so no `mark` literal in between -- the type IS the leaf.
//  Their sum is the 气道's path marks; that row is read-only over in CmdPath. See CLAUDE.md "Qi".
public final class CmdQi {

    private CmdQi() {}

    private static final String ARG_TYPE = "type";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("qi")
                .then(ModEnumArgument.arg(ARG_TYPE, QiType.values())
                        .then(markNode("set", QiService::setMark))
                        .then(markNode("add", QiService::addMark))
                        .then(markNode("sub", (player, type, value) -> QiService.addMark(player, type, -value))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> markNode(String literal, QiOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    QiType type = ModEnumArgument.get(context, ARG_TYPE, QiType.values());
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, type, value));
                }));
    }

    @FunctionalInterface
    private interface QiOperation {
        void apply(ServerPlayer player, QiType type, long value);
    }
}

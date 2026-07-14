package com.unknown.guzhenren.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.CoreService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

//  Shared by every Cmd*: optional [targets], the awakening gate, the per-target apply.
//  See CLAUDE.md "The awakening gate".
public final class ModCommandSupport {

    private ModCommandSupport() {}

    public static final String ARG_TARGETS = "targets";
    public static final String ARG_VALUE = "value";

    public static final String FAILED_AWAKENED = "guzhenren.command.failed.awakened";
    public static final String FAILED_UNAWAKENED = "guzhenren.command.failed.unawakened";

    public static final Predicate<ServerPlayer> ANYONE = player -> true;
    public static final Predicate<ServerPlayer> AWAKENED = player -> CoreService.get(player).isAwakened();

    //  Presentation only -- requires() resolves before [targets] is parsed, so it sees only the caller.
    public static boolean sourceAwakened(CommandSourceStack source) {
        return !(source.getEntity() instanceof ServerPlayer player) || CoreService.get(player).isAwakened();
    }

    //region node builders

    //  Executor on both the bare node and node-plus-targets -- Brigadier has no optional-arg spelling.
    public static ArgumentBuilder<CommandSourceStack, ?> withTargets(
            ArgumentBuilder<CommandSourceStack, ?> node, Command<CommandSourceStack> executor) {
        return node.executes(executor)
                .then(Commands.argument(ARG_TARGETS, EntityArgument.players()).executes(executor));
    }

    //  `<literal> <long> [targets]`, ungated -- soul, lifespan, wisdom: every mortal has those.
    public static ArgumentBuilder<CommandSourceStack, ?> longNode(String literal, LongOperation operation) {
        return longNode(literal, operation, ANYONE, null);
    }

    public static ArgumentBuilder<CommandSourceStack, ?> longNode(
            String literal, LongOperation operation, Predicate<ServerPlayer> allowed, String refusedKey) {
        return Commands.literal(literal).then(withTargets(
                Commands.argument(ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    long value = LongArgumentType.getLong(context, ARG_VALUE);
                    return applyIf(context, allowed, refusedKey, player -> operation.apply(player, value));
                }));
    }

    //  `<literal> set <enum> [targets]`.
    public static <E extends Enum<E> & StringRepresentable> ArgumentBuilder<CommandSourceStack, ?> enumSetNode(
            String literal, E[] values, EnumOperation<E> operation,
            Predicate<ServerPlayer> allowed, String refusedKey) {
        return Commands.literal(literal).then(Commands.literal("set")
                .then(withTargets(ModEnumArgument.arg(ARG_VALUE, values), context -> {
                    E value = ModEnumArgument.get(context, ARG_VALUE, values);
                    return applyIf(context, allowed, refusedKey, player -> operation.apply(player, value));
                })));
    }

    //endregion
    //region execution

    public static int apply(CommandContext<CommandSourceStack> context, PlayerOperation operation)
            throws CommandSyntaxException {
        return applyIf(context, ANYONE, null, operation);
    }

    public static int applyOnAwakened(CommandContext<CommandSourceStack> context, PlayerOperation operation)
            throws CommandSyntaxException {
        return applyIf(context, AWAKENED, FAILED_UNAWAKENED, operation);
    }

    //  Runs on every allowed target, names the rest in one red line. A refusal is not an error.
    public static int applyIf(CommandContext<CommandSourceStack> context, Predicate<ServerPlayer> allowed,
            String refusedKey, PlayerOperation operation) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        List<ServerPlayer> refused = new ArrayList<>();
        int updated = 0;

        for (ServerPlayer player : targets(context)) {
            if (!allowed.test(player)) {
                refused.add(player);
                continue;
            }
            operation.apply(player);
            updated++;
        }

        if (!refused.isEmpty()) {
            ModCommandFeedback.failure(source, Component.translatable(refusedKey, names(refused)));
        }
        if (updated > 0) {
            ModCommandFeedback.success(source, Component.translatable("guzhenren.command.updated", updated));
        }
        return updated;
    }

    //  No targets means "me". getPlayerOrException is what refuses the console politely.
    public static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        boolean explicit = context.getNodes().stream()
                .anyMatch(node -> node.getNode().getName().equals(ARG_TARGETS));

        return explicit
                ? EntityArgument.getPlayers(context, ARG_TARGETS)
                : List.of(context.getSource().getPlayerOrException());
    }

    private static Component names(List<ServerPlayer> players) {
        return ComponentUtils.formatList(players, ServerPlayer::getDisplayName);
    }

    //endregion

    @FunctionalInterface
    public interface PlayerOperation {
        void apply(ServerPlayer player) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface EnumOperation<E extends Enum<E>> {
        void apply(ServerPlayer player, E value);
    }

    @FunctionalInterface
    public interface LongOperation {
        void apply(ServerPlayer player, long value);
    }

    @FunctionalInterface
    public interface IntOperation {
        void apply(ServerPlayer player, int value);
    }
}

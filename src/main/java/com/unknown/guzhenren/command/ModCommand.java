package com.unknown.guzhenren.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.data.PathEntry;
import com.unknown.guzhenren.attachment.data.SoulData;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.attachment.service.LifespanService;
import com.unknown.guzhenren.attachment.service.PathService;
import com.unknown.guzhenren.attachment.service.PlayerDataService;
import com.unknown.guzhenren.attachment.service.SoulService;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import com.unknown.guzhenren.custom.enums.core.GuRank;
import com.unknown.guzhenren.custom.enums.core.GuStage;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.GuPathAttainment;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

//  /guzhenren (alias /gzr) -- read and edit the five player-data systems.
//
//  Every leaf acts on the caller, or on [targets] if an entity selector is supplied. That is done
//  by hanging the same executor off both the value node and the targets node (see withTargets),
//  which is Brigadier's only way to spell "optional trailing argument".
@EventBusSubscriber(modid = Guzhenren.MOD_ID)
public final class ModCommand {

    private ModCommand() {}

    private static final int PERMISSION_LEVEL = 2;

    private static final String ARG_TARGETS = "targets";
    private static final String ARG_VALUE = "value";
    private static final String ARG_PATH = "path";

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(
                Commands.literal("guzhenren")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .then(withTargets(Commands.literal("info"), ModCommand::info))
                        .then(withTargets(Commands.literal("reset"),
                                context -> apply(context, PlayerDataService::resetAll)))
                        .then(core())
                        .then(essence())
                        .then(lifespan())
                        .then(soul())
                        .then(path()));

        dispatcher.register(Commands.literal("gzr")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .redirect(root));
    }

    //region tree

    private static ArgumentBuilder<CommandSourceStack, ?> core() {
        return Commands.literal("core")
                .then(Commands.literal("rank").then(Commands.literal("set")
                        .then(withTargets(ModEnumArgument.arg(ARG_VALUE, GuRank.values()), context -> {
                            GuRank rank = ModEnumArgument.get(context, ARG_VALUE, GuRank.values());
                            return apply(context, player -> CoreService.setRank(player, rank));
                        }))))
                .then(Commands.literal("stage").then(Commands.literal("set")
                        .then(withTargets(ModEnumArgument.arg(ARG_VALUE, GuStage.values()), context -> {
                            GuStage stage = ModEnumArgument.get(context, ARG_VALUE, GuStage.values());
                            return apply(context, player -> CoreService.setStage(player, stage));
                        }))))
                .then(Commands.literal("physique").then(Commands.literal("set")
                        .then(withTargets(ModEnumArgument.arg(ARG_VALUE, GuExtremePhysique.values()), context -> {
                            GuExtremePhysique physique =
                                    ModEnumArgument.get(context, ARG_VALUE, GuExtremePhysique.values());
                            return apply(context, player -> CoreService.setExtremePhysique(player, physique));
                        }))))
                .then(Commands.literal("lifestate").then(Commands.literal("set")
                        .then(withTargets(ModEnumArgument.arg(ARG_VALUE, GuLifeState.values()), context -> {
                            GuLifeState state = ModEnumArgument.get(context, ARG_VALUE, GuLifeState.values());
                            return apply(context, player -> CoreService.setLifeState(player, state));
                        }))))
                .then(Commands.literal("base")
                        .then(baseOp("set", CoreService::setBaseEssence))
                        .then(baseOp("add", CoreService::addBaseEssence)))
                .then(withTargets(Commands.literal("awaken"),
                        context -> apply(context, CoreService::awaken)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> essence() {
        return Commands.literal("essence")
                .then(longOp("set", (context, player, value) -> EssenceService.set(player, value)))
                .then(longOp("add", (context, player, value) -> EssenceService.add(player, value)))
                .then(withTargets(Commands.literal("refill"),
                        context -> apply(context, EssenceService::refill)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> lifespan() {
        return Commands.literal("lifespan")
                .then(Commands.literal("age")
                        .then(longOp("set", (context, player, value) -> LifespanService.setAge(player, value)))
                        .then(longOp("add", (context, player, value) -> LifespanService.addAge(player, value))))
                .then(Commands.literal("lifespan")
                        .then(longOp("set", (context, player, value) -> LifespanService.setLifespan(player, value)))
                        .then(longOp("add", (context, player, value) -> LifespanService.addLifespan(player, value))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> soul() {
        return Commands.literal("soul")
                .then(Commands.literal("max")
                        .then(longOp("set", (context, player, value) -> SoulService.setMax(player, value)))
                        .then(longOp("add", (context, player, value) -> SoulService.addMax(player, value))))
                .then(Commands.literal("current")
                        .then(longOp("set", (context, player, value) -> SoulService.setCurrent(player, value)))
                        .then(longOp("add", (context, player, value) -> SoulService.addCurrent(player, value))))
                .then(withTargets(Commands.literal("refill"),
                        context -> apply(context, SoulService::refill)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> path() {
        return Commands.literal("path").then(ModEnumArgument.arg(ARG_PATH, GuPath.values())
                .then(Commands.literal("attainment").then(Commands.literal("set")
                        .then(withTargets(ModEnumArgument.arg(ARG_VALUE, GuPathAttainment.values()), context -> {
                            GuPath path = pathOf(context);
                            GuPathAttainment attainment =
                                    ModEnumArgument.get(context, ARG_VALUE, GuPathAttainment.values());
                            return apply(context, player -> PathService.setAttainment(player, path, attainment));
                        }))))
                .then(Commands.literal("mark")
                        .then(longOp("set", (context, player, value) ->
                                PathService.setMark(player, pathOf(context), value)))
                        .then(longOp("add", (context, player, value) ->
                                PathService.addMark(player, pathOf(context), value)))));
    }

    //  Every leaf under /gzr path <path> ... has to re-read the path off the same node.
    private static GuPath pathOf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ModEnumArgument.get(context, ARG_PATH, GuPath.values());
    }

    //endregion
    //region builders

    //  Hangs the executor off both the bare node and the node-plus-targets, which is what makes
    //  [targets] optional. Brigadier has no other spelling for that.
    private static ArgumentBuilder<CommandSourceStack, ?> withTargets(
            ArgumentBuilder<CommandSourceStack, ?> node, Command<CommandSourceStack> executor) {
        return node.executes(executor)
                .then(Commands.argument(ARG_TARGETS, EntityArgument.players()).executes(executor));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> longOp(String literal, LongOp op) {
        return Commands.literal(literal).then(withTargets(
                Commands.argument(ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    long value = LongArgumentType.getLong(context, ARG_VALUE);
                    return apply(context, player -> op.apply(context, player, value));
                }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> baseOp(String literal, IntOp op) {
        return Commands.literal(literal).then(withTargets(
                Commands.argument(ARG_VALUE, IntegerArgumentType.integer(-CoreData.MAX_BASE, CoreData.MAX_BASE)),
                context -> {
                    int value = IntegerArgumentType.getInteger(context, ARG_VALUE);
                    return apply(context, player -> op.apply(player, value));
                }));
    }

    //endregion
    //region execution

    private static int apply(CommandContext<CommandSourceStack> context, PlayerOp op) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = targets(context);
        for (ServerPlayer player : targets) {
            op.apply(player);
        }

        int count = targets.size();
        context.getSource().sendSuccess(() -> Component.translatable("guzhenren.command.updated", count), true);
        return count;
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : targets(context)) {
            CoreData core = CoreService.get(player);
            SoulData soul = SoulService.get(player);

            source.sendSuccess(() -> Component.translatable(
                    "guzhenren.command.info.header", player.getDisplayName()), false);

            source.sendSuccess(() -> Component.translatable("guzhenren.command.info.realm",
                    Component.translatable(core.rank().getTranslationKey()),
                    Component.translatable(core.stage().getTranslationKey()),
                    Component.translatable(core.lifeForm().getTranslationKey())), false);

            source.sendSuccess(() -> Component.translatable("guzhenren.command.info.talent",
                    Component.translatable(core.talent().getTranslationKey()),
                    core.baseEssence()), false);

            if (core.extremePhysique() != GuExtremePhysique.NONE) {
                source.sendSuccess(() -> Component.translatable("guzhenren.command.info.physique",
                        Component.translatable(core.extremePhysique().getTranslationKey())), false);
            }

            source.sendSuccess(() -> Component.translatable("guzhenren.command.info.essence",
                    EssenceService.current(player), EssenceService.maxEssence(core)), false);

            source.sendSuccess(() -> Component.translatable("guzhenren.command.info.lifespan",
                    LifespanService.get(player).age(), LifespanService.get(player).lifespan()), false);

            source.sendSuccess(() -> Component.translatable("guzhenren.command.info.soul",
                    Component.translatable(soul.tier().getTranslationKey()),
                    soul.currentSoul(), soul.maxSoul()), false);

            //  PathData is sparse, so an empty map really does mean "has not touched a single path".
            var entries = PathService.get(player).entries();
            if (entries.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("guzhenren.command.info.no_path"), false);
            } else {
                source.sendSuccess(() -> Component.translatable("guzhenren.command.info.paths"), false);
                entries.forEach((path, entry) -> source.sendSuccess(() -> pathLine(path, entry), false));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static Component pathLine(GuPath path, PathEntry entry) {
        return Component.translatable("guzhenren.command.info.path_entry",
                Component.translatable(path.getTranslationKey()),
                Component.translatable(entry.attainment().getTranslationKey()),
                entry.mark());
    }

    //  No targets given means "me". getPlayerOrException is what refuses the console politely.
    private static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        boolean explicit = context.getNodes().stream()
                .anyMatch(node -> node.getNode().getName().equals(ARG_TARGETS));

        return explicit
                ? EntityArgument.getPlayers(context, ARG_TARGETS)
                : List.of(context.getSource().getPlayerOrException());
    }

    //endregion

    @FunctionalInterface
    private interface PlayerOp {
        void apply(ServerPlayer player) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface LongOp {
        void apply(CommandContext<CommandSourceStack> context, ServerPlayer player, long value)
                throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface IntOp {
        void apply(ServerPlayer player, int value);
    }
}

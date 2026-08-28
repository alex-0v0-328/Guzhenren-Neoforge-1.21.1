package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.data.aperture.ApertureData;
import com.unknown.guzhenren.attachment.service.aperture.ApertureEssenceService;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

/**
 * {@code /gzr aperture [1..2]}: reads and writes one aperture [空窍] of the holder.
 *
 * <p>Guarded by a {@code requires(sourceAwakened)} gate (presentation only) AND an
 * {@code applyOnAperture} per-target gate (data protection). An optional integer index right after the
 * literal picks the aperture (1-based, default the first) -- safe next to a literal, unlike a word
 * argument. Offers graded setters for rank, stage, and talent, the essence subtree with {@code base}/
 * {@code current}/{@code distilled} and their {@code refill} where applicable. The physique subtree
 * hangs off the unindexed branch only: ten-extremes belong to the primary aperture alone.
 *
 * @author Alex
 * @version 1.0.0
 * @see ModCommandSupport
 * @since 1.0.0
 */

public final class CmdAperture {

    private CmdAperture() {}

    private static final String ARG_APERTURE = "aperture";
    private static final String FAILED_INDEX = "guzhenren.command.failed.aperture_index";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("aperture").requires(ModCommandSupport::sourceAwakened);

        attachWrites(root, true);
        attachWrites(root.then(Commands.argument(ARG_APERTURE,
                IntegerArgumentType.integer(1, ApertureData.MAX_APERTURES))), false);
        return root;
    }

    private static void attachWrites(ArgumentBuilder<CommandSourceStack, ?> parent, boolean withPhysique) {
        parent.then(graded("rank", Rank.settable(),
                ApertureService::setRank, ApertureService::shiftRank))
                .then(graded("stage", Stage.settable(),
                        ApertureService::setStage, ApertureService::shiftStage))
                .then(graded("talent", Talent.settable(),
                        ApertureService::setTalent, ApertureService::shiftTalent))
                .then(essence());
        if (withPhysique) {
            parent.then(Commands.literal("physique")
                    .then(ModCommandSupport.enumSetNode("extreme", ExtremePhysique.values(),
                            ApertureService::setExtremePhysique,
                            ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED)));
        }
    }

    private static ArgumentBuilder<CommandSourceStack, ?> essence() {
        return Commands.literal("essence")
                .then(Commands.literal("base")
                        .then(baseNode("set", ApertureService::setBaseEssence))
                        .then(baseNode("add", ApertureService::addBaseEssence))
                        .then(baseNode("sub", (p, i, v) -> ApertureService.addBaseEssence(p, i, -v))))
                .then(Commands.literal("current")
                        .then(currentNode("set", ApertureEssenceService::set))
                        .then(currentNode("add",
                                (p, i, v) -> ApertureEssenceService.set(p, i,
                                        ApertureService.aperture(p, i).currentEssence() + v)))
                        .then(currentNode("sub",
                                (p, i, v) -> ApertureEssenceService.set(p, i,
                                        ApertureService.aperture(p, i).currentEssence() - v))))
                .then(Commands.literal("distilled")
                        .then(currentNode("set", ApertureEssenceService::setDistilled))
                        .then(currentNode("add",
                                (p, i, v) -> ApertureEssenceService.setDistilled(p, i,
                                        ApertureService.aperture(p, i).distilledEssence() + v)))
                        .then(currentNode("sub",
                                (p, i, v) -> ApertureEssenceService.setDistilled(p, i,
                                        ApertureService.aperture(p, i).distilledEssence() - v))))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.applyOnAwakened(context, ApertureEssenceService::refill)));
    }

    //region builders

    private static int apertureOf(CommandContext<CommandSourceStack> context) {
        boolean indexed = context.getNodes().stream()
                .anyMatch(node -> node.getNode().getName().equals(ARG_APERTURE));
        return indexed ? IntegerArgumentType.getInteger(context, ARG_APERTURE) - 1
                : ApertureData.PRIMARY;
    }

    private static int applyOnAperture(CommandContext<CommandSourceStack> context, Indexed operation)
            throws CommandSyntaxException {
        int index = apertureOf(context);
        String refused = index == ApertureData.PRIMARY
                ? ModCommandSupport.FAILED_UNAWAKENED : FAILED_INDEX;
        return ModCommandSupport.applyIf(context,
                ModCommandSupport.AWAKENED.and(p -> index < ApertureService.get(p).count()),
                refused, player -> operation.apply(player, index));
    }

    @FunctionalInterface
    private interface Indexed {
        void apply(ServerPlayer player, int aperture);
    }

    @FunctionalInterface
    private interface EnumOp<E extends Enum<E>> {
        void apply(ServerPlayer player, int aperture, E value);
    }

    @FunctionalInterface
    private interface IntOp {
        void apply(ServerPlayer player, int aperture, int value);
    }

    @FunctionalInterface
    private interface LongOp {
        void apply(ServerPlayer player, int aperture, long value);
    }

    private static <E extends Enum<E> & StringRepresentable> ArgumentBuilder<CommandSourceStack, ?> graded(
            String literal, E[] settable, EnumOp<E> set, IntOp shift) {
        return Commands.literal(literal)
                .then(Commands.literal("set")
                        .then(ModCommandSupport.withTargets(
                                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, settable), context -> {
                                    E value = ModEnumArgument.get(context, ModCommandSupport.ARG_VALUE, settable);
                                    return applyOnAperture(context,
                                            (player, aperture) -> set.apply(player, aperture, value));
                                })))
                .then(shiftNode("up", 1, shift))
                .then(shiftNode("down", -1, shift));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> shiftNode(String literal, int delta, IntOp shift) {
        return ModCommandSupport.withTargets(Commands.literal(literal),
                context -> applyOnAperture(context, (player, aperture) -> shift.apply(player, aperture, delta)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> currentNode(String literal, LongOp operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, LongArgumentType.longArg()),
                context -> {
                    long value = LongArgumentType.getLong(context, ModCommandSupport.ARG_VALUE);
                    return applyOnAperture(context, (player, aperture) -> operation.apply(player, aperture, value));
                }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> baseNode(String literal, IntOp operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE,
                        IntegerArgumentType.integer(-Aperture.MAX_BASE, Aperture.MAX_BASE)),
                context -> {
                    int value = IntegerArgumentType.getInteger(context, ModCommandSupport.ARG_VALUE);
                    return applyOnAperture(context, (player, aperture) -> operation.apply(player, aperture, value));
                }));
    }

    //endregion
}

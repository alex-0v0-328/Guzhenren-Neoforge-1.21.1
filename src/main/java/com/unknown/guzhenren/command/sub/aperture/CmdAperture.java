package com.unknown.guzhenren.command.sub.aperture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.data.aperture.Aperture;
import com.unknown.guzhenren.attachment.service.aperture.ApertureService;
import com.unknown.guzhenren.attachment.service.aperture.EssenceService;
import com.unknown.guzhenren.command.ModCommandSupport.EnumOperation;
import com.unknown.guzhenren.command.ModCommandSupport.IntOperation;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.aperture.ApertureState;
import com.unknown.guzhenren.custom.enums.aperture.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Stage;
import com.unknown.guzhenren.custom.enums.aperture.Talent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

//  /gzr aperture lifestate set <v>
//  /gzr aperture rank|stage|talent  set <v> | up | down
//  /gzr aperture physique extreme   set <v>
//  /gzr aperture essence base|current set|add|sub <n> | essence refill
//  The whole branch is gated -- a rank on a body with no aperture is the bad data the gate exists to
//  stop. `awaken` lives at the root, which is what lets that be ONE requires() here.
//  No `max` leaf: the cap is derived. See CLAUDE.md "The awakening gate".
public final class CmdAperture {

    private CmdAperture() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("aperture")
                .requires(ModCommandSupport::sourceAwakened)
                .then(ModCommandSupport.enumSetNode("lifestate", ApertureState.values(),
                        ApertureService::setState, ModCommandSupport.AWAKENED,
                        ModCommandSupport.FAILED_UNAWAKENED))
                .then(graded("rank", Rank.settable(),
                        ApertureService::setRank, ApertureService::shiftRank))
                .then(graded("stage", Stage.settable(),
                        ApertureService::setStage, ApertureService::shiftStage))
                .then(graded("talent", Talent.settable(),
                        ApertureService::setTalent, ApertureService::shiftTalent))
                .then(physique())
                .then(essence());
    }

    //  `extreme` is one kind of physique among future others, so the kind gets its own node.
    private static ArgumentBuilder<CommandSourceStack, ?> physique() {
        return Commands.literal("physique")
                .then(ModCommandSupport.enumSetNode("extreme", ExtremePhysique.values(),
                        ApertureService::setExtremePhysique,
                        ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> essence() {
        return Commands.literal("essence")
                .then(Commands.literal("base")
                        .then(baseNode("set", ApertureService::setBaseEssence))
                        .then(baseNode("add", ApertureService::addBaseEssence))
                        .then(baseNode("sub", (p, v) -> ApertureService.addBaseEssence(p, -v))))
                .then(Commands.literal("current")
                        .then(currentNode("set", EssenceService::set))
                        .then(currentNode("add", EssenceService::add))
                        .then(currentNode("sub", (p, v) -> EssenceService.add(p, -v))))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.applyOnAwakened(context, EssenceService::refill)));
    }

    //region builders

    //  The branch's requires() is presentation only -- it cannot see [targets]. applyOnAwakened, on every
    //  leaf below, is what actually protects the data.
    private static <E extends Enum<E> & StringRepresentable> ArgumentBuilder<CommandSourceStack, ?> graded(
            String literal, E[] settable, EnumOperation<E> set, IntOperation shift) {
        return Commands.literal(literal)
                .then(Commands.literal("set")
                        .then(ModCommandSupport.withTargets(
                                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, settable), context -> {
                                    E value = ModEnumArgument.get(context, ModCommandSupport.ARG_VALUE, settable);
                                    return ModCommandSupport.applyOnAwakened(context,
                                            player -> set.apply(player, value));
                                })))
                .then(shiftNode("up", 1, shift))
                .then(shiftNode("down", -1, shift));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> shiftNode(String literal, int delta, IntOperation shift) {
        return ModCommandSupport.withTargets(Commands.literal(literal),
                context -> ModCommandSupport.applyOnAwakened(context, player -> shift.apply(player, delta)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> currentNode(
            String literal, ModCommandSupport.LongOperation operation) {
        return ModCommandSupport.longNode(literal, operation,
                ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> baseNode(String literal, IntOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE,
                        IntegerArgumentType.integer(-Aperture.MAX_BASE, Aperture.MAX_BASE)),
                context -> {
                    int value = IntegerArgumentType.getInteger(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.applyOnAwakened(context, player -> operation.apply(player, value));
                }));
    }

    //endregion
}

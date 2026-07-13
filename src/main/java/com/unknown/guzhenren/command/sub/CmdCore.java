package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModCommandSupport.EnumOperation;
import com.unknown.guzhenren.command.ModCommandSupport.IntOperation;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.core.GuRank;
import com.unknown.guzhenren.custom.enums.core.GuStage;
import com.unknown.guzhenren.custom.enums.core.GuTalent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.StringRepresentable;

//  /gzr core rank|stage|talent  set <v> | up | down
//
//  The three graded dials. Positive delta is always "better" -- each enum owns which way that is and
//  where it stops (see CLAUDE.md "Bounds"; GuTalent's direction is the backwards one).
public final class CmdCore {

    private CmdCore() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("core")
                .requires(ModCommandSupport::sourceAwakened)
                .then(graded("rank", GuRank.settable(), CoreService::setRank, CoreService::shiftRank))
                .then(graded("stage", GuStage.settable(), CoreService::setStage, CoreService::shiftStage))
                .then(graded("talent", GuTalent.settable(), CoreService::setTalent, CoreService::shiftTalent));
    }

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
}

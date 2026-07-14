package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.data.CoreData;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.attachment.service.EssenceService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModCommandSupport.IntOperation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr essence base set|add|sub <int> | current set|add|sub <long> | refill
//
//  Gated: on an unawakened target the cap is 0, so a write would clamp to nothing and still report
//  success. Refuse out loud instead.
//
//  No `current max` leaf: maxEssence is derived (base × stage × rank), sized by `base` and the core.
//  `base` writes CoreData.baseEssence -- it lives here because sizing the pool is all it does.
public final class CmdEssence {

    private CmdEssence() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("essence")
                .requires(ModCommandSupport::sourceAwakened)
                .then(Commands.literal("base")
                        .then(baseNode("set", CoreService::setBaseEssence))
                        .then(baseNode("add", CoreService::addBaseEssence))
                        .then(baseNode("sub", (p, v) -> CoreService.addBaseEssence(p, -v))))
                .then(Commands.literal("current")
                        .then(gated("set", EssenceService::set))
                        .then(gated("add", EssenceService::add))
                        .then(gated("sub", (p, v) -> EssenceService.add(p, -v))))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.applyOnAwakened(context, EssenceService::refill)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> gated(
            String literal, ModCommandSupport.LongOperation operation) {
        return ModCommandSupport.longNode(literal, operation,
                ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> baseNode(String literal, IntOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE,
                        IntegerArgumentType.integer(-CoreData.MAX_BASE, CoreData.MAX_BASE)),
                context -> {
                    int value = IntegerArgumentType.getInteger(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.applyOnAwakened(context, player -> operation.apply(player, value));
                }));
    }
}

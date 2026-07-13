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

//  /gzr essence base set|add <int> | current set|add <long> | refill
//
//  Gated: the pool's cap is derived from the core, so on an unawakened target it is 0 and a write
//  would clamp to nothing -- and then report success. Refuse it out loud instead.
//
//  `base` writes CoreData.baseEssence; it lives under essence because that is the only thing it does.
public final class CmdEssence {

    private CmdEssence() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("essence")
                .requires(ModCommandSupport::sourceAwakened)
                .then(Commands.literal("base")
                        .then(baseNode("set", CoreService::setBaseEssence))
                        .then(baseNode("add", CoreService::addBaseEssence)))
                .then(Commands.literal("current")
                        .then(gated("set", EssenceService::set))
                        .then(gated("add", EssenceService::add)))
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

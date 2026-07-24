package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unknown.guzhenren.attachment.service.body.StrengthService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.strength.BeastStrength;
import com.unknown.guzhenren.custom.enums.strength.HumanStrength;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

//  /gzr body strength -- the two branches take different verbs, because they are different shapes:
//  the beast branch is set membership (grant/revoke/clear), the Human Jun branch a count (set/add/sub).
//  ⚠ Every enum argument hangs under a literal, never beside one: a word argument swallows a sibling.
public final class CmdStrength {

    private CmdStrength() {}

    private static final String ARG_KIND = "kind";

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("strength")
                .then(beastNode("grant", StrengthService::grant))
                .then(beastNode("revoke", StrengthService::revoke))
                .then(humanStrength())
                .then(ModCommandSupport.withTargets(Commands.literal("clear"),
                        context -> ModCommandSupport.apply(context, StrengthService::clear)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beastNode(
            String literal, ModCommandSupport.EnumOperation<BeastStrength> operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, BeastStrength.values()),
                context -> {
                    BeastStrength beast = ModEnumArgument.get(
                            context, ModCommandSupport.ARG_VALUE, BeastStrength.values());
                    return ModCommandSupport.apply(context, player -> operation.apply(player, beast));
                }));
    }

    //  A raw count, so set/add/sub -- StrengthData clamps it to nine per kind on the way in.
    private static ArgumentBuilder<CommandSourceStack, ?> humanStrength() {
        return Commands.literal("human")
                .then(ModEnumArgument.arg(ARG_KIND, HumanStrength.values())
                        .then(countNode("set", StrengthService::setHumanStrength))
                        .then(countNode("add", StrengthService::addHumanStrength))
                        .then(countNode("sub", (p, k, v) -> StrengthService.addHumanStrength(p, k, -v))));
    }

    //  ⚠ The kind reads back under its OWN argument name -- ARG_VALUE is already taken by the count.
    private static ArgumentBuilder<CommandSourceStack, ?> countNode(String literal, HumanStrengthOperation operation) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                Commands.argument(ModCommandSupport.ARG_VALUE, IntegerArgumentType.integer()),
                context -> {
                    HumanStrength kind = kindOf(context);
                    int value = IntegerArgumentType.getInteger(context, ModCommandSupport.ARG_VALUE);
                    return ModCommandSupport.apply(context, player -> operation.apply(player, kind, value));
                }));
    }

    private static HumanStrength kindOf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ModEnumArgument.get(context, ARG_KIND, HumanStrength.values());
    }

    @FunctionalInterface
    private interface HumanStrengthOperation {
        void apply(ServerPlayer player, HumanStrength kind, int value);
    }
}

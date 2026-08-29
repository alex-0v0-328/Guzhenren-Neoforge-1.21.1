package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.body.ExtremePhysique;
import com.unknown.guzhenren.custom.enums.body.Physique;
import com.unknown.guzhenren.custom.enums.body.Race;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

/**
 * {@code /gzr body}: reads and writes body [肉身] state -- physiques, race, lifespan and age.
 *
 * <p>Assembles the body subtree under {@code /gzr body}: physiques, race, lifespan and age. The
 * path domain writes live under {@code /gzr path}. All of {@code /gzr body} is ungated -- a
 * mortal ages and changes form too.
 *
 *
 * @author Alex
 * @version 1.0.0
 * @see com.unknown.guzhenren.command.ModCommandSupport
 * @since 1.0.0
 */

public final class CmdBody {

    private CmdBody() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("body")
                .then(physique())
                .then(ModCommandSupport.enumSetNode("race", Race.values(),
                        BodyService::setRace, ModCommandSupport.ANYONE, null))
                .then(ModCommandSupport.counter("lifespan", BodyService::setLifespan, BodyService::addLifespan))
                .then(ModCommandSupport.counter("age", BodyService::setAge, BodyService::addAge));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> physique() {
        return Commands.literal("physique")
                .then(enumAction("add", Physique.values(), (player, value) -> BodyService.addPhysique(player, value),
                        value -> value != Physique.EXTREME, player -> true,
                        "guzhenren.command.failed.extreme_physique_required"))
                .then(enumAction("remove", Physique.values(),
                        (player, value) -> BodyService.removePhysique(player, value), value -> true,
                        player -> true, null))
                .then(Commands.literal("extreme")
                        .then(enumAction("set", ExtremePhysique.settable(),
                                (player, value) -> BodyService.setExtremePhysique(player, value),
                                value -> true, ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED)));
    }

    private static <E extends Enum<E> & StringRepresentable> ArgumentBuilder<CommandSourceStack, ?> enumAction(
            String literal, E[] values, ModCommandSupport.EnumOperation<E> operation,
            Predicate<E> valueAllowed, Predicate<ServerPlayer> allowed, String refusedKey) {
        return Commands.literal(literal).then(ModCommandSupport.withTargets(
                ModEnumArgument.arg(ModCommandSupport.ARG_VALUE, values), context -> {
                    E value = ModEnumArgument.get(context, ModCommandSupport.ARG_VALUE, values);
                    boolean valid = valueAllowed.test(value);
                    Predicate<ServerPlayer> gate = !valid
                            ? player -> false : allowed;
                    String key = !valid
                            ? "guzhenren.command.failed.extreme_physique_required" : refusedKey;
                    return ModCommandSupport.applyIf(context, gate, key, player -> operation.apply(player, value));
                }));
    }
}

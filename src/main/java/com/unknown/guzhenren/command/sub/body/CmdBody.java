package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.attachment.service.body.StaminaService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.Race;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CmdBody {

    private CmdBody() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("body")
                .then(ModCommandSupport.enumSetNode("lifeform", LifeForm.values(),
                        BodyService::setLifeForm, ModCommandSupport.ANYONE, null))
                .then(ModCommandSupport.enumSetNode("race", Race.values(),
                        BodyService::setRace, ModCommandSupport.ANYONE, null))
                .then(soul())
                .then(stamina())
                .then(counter("lifespan", BodyService::setLifespan, BodyService::addLifespan))
                .then(counter("age", BodyService::setAge, BodyService::addAge))
                .then(CmdPath.node())
                .then(CmdQi.node())
                .then(CmdStrength.node());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> soul() {
        return Commands.literal("soul")
                .then(counter("max", SoulService::setMax, SoulService::addMax))
                .then(counter("current", SoulService::setCurrent, SoulService::addCurrent))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.apply(context, SoulService::refill)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> stamina() {
        return Commands.literal("stamina")
                .then(counter("current", StaminaService::setCurrent, StaminaService::addCurrent))
                .then(counter("bonus", StaminaService::setBonus, StaminaService::addBonus))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.apply(context, StaminaService::refill)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> counter(
            String literal, ModCommandSupport.LongOperation set, ModCommandSupport.LongOperation add) {
        return Commands.literal(literal)
                .then(ModCommandSupport.longNode("set", set))
                .then(ModCommandSupport.longNode("add", add))
                .then(ModCommandSupport.longNode("sub", (p, v) -> add.apply(p, -v)));
    }
}

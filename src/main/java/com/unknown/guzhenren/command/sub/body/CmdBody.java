package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.attachment.service.body.SoulService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.LifeState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr body -- lifestate, lifeform, soul, lifespan/age, plus qi (CmdQi) and path (CmdPath).
//  Ungated throughout: a mortal has soul, ages, dies and can be zombified without an aperture.
public final class CmdBody {

    private CmdBody() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("body")
                .then(ModCommandSupport.enumSetNode("lifestate", LifeState.values(),
                        BodyService::setLifeState, ModCommandSupport.ANYONE, null))
                .then(ModCommandSupport.enumSetNode("lifeform", LifeForm.values(),
                        BodyService::setLifeForm, ModCommandSupport.ANYONE, null))
                .then(soul())
                .then(counter("lifespan", BodyService::setLifespan, BodyService::addLifespan))
                .then(counter("age", BodyService::setAge, BodyService::addAge))
                .then(CmdQi.node())
                .then(CmdPath.node())
                .then(CmdStrength.node());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> soul() {
        return Commands.literal("soul")
                .then(counter("max", SoulService::setMax, SoulService::addMax))
                .then(counter("current", SoulService::setCurrent, SoulService::addCurrent))
                .then(ModCommandSupport.withTargets(Commands.literal("refill"),
                        context -> ModCommandSupport.apply(context, SoulService::refill)));
    }

    //  A raw count: set / add / sub, and `sub n` is `add -n`. No up/down -- those belong to graded enums.
    private static ArgumentBuilder<CommandSourceStack, ?> counter(
            String literal, ModCommandSupport.LongOperation set, ModCommandSupport.LongOperation add) {
        return Commands.literal(literal)
                .then(ModCommandSupport.longNode("set", set))
                .then(ModCommandSupport.longNode("add", add))
                .then(ModCommandSupport.longNode("sub", (p, v) -> add.apply(p, -v)));
    }
}

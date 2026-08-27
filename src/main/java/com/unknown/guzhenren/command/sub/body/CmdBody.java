package com.unknown.guzhenren.command.sub.body;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.body.BodyService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.body.LifeForm;
import com.unknown.guzhenren.custom.enums.body.Race;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * {@code /gzr body}: reads and writes body [肉身] state -- life form, race, lifespan and age.
 *
 * <p>Assembles the body subtree under {@code /gzr body}, delegating to
 * {@link com.unknown.guzhenren.command.sub.body.CmdPath},
 * {@link com.unknown.guzhenren.command.sub.body.CmdQi}, and
 * {@link com.unknown.guzhenren.command.sub.body.CmdStrength} for the deeper branches. All of
 * {@code /gzr body} is ungated -- a mortal ages and walks a path too.
 *
 * <p>⚠ Setting the zombie [僵] form from here leaves the tier unset, so a zombie made by command
 * carries no attack bonus. That is this command's shape, not a fault in the bonus.
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
                .then(ModCommandSupport.enumSetNode("lifeform", LifeForm.values(),
                        BodyService::setLifeForm, ModCommandSupport.ANYONE, null))
                .then(ModCommandSupport.enumSetNode("race", Race.values(),
                        BodyService::setRace, ModCommandSupport.ANYONE, null))
                .then(ModCommandSupport.counter("lifespan", BodyService::setLifespan, BodyService::addLifespan))
                .then(ModCommandSupport.counter("age", BodyService::setAge, BodyService::addAge))
                .then(CmdPath.node())
                .then(CmdQi.node())
                .then(CmdStrength.node());
    }
}

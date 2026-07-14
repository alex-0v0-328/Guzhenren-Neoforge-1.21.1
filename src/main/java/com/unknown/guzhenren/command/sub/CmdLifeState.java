package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import net.minecraft.commands.CommandSourceStack;

//  /gzr lifestate set <v>
//
//  Ungated, and top level rather than under core: 生 / 僵 / 死 is a fact about a body, and a body needs
//  no aperture to be turned into a zombie. It only *reads* as cultivation because a zombified aperture
//  is what stops essence regen -- see GuLifeState.
public final class CmdLifeState {

    private CmdLifeState() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return ModCommandSupport.enumSetNode("lifestate", GuLifeState.values(), CoreService::setLifeState,
                ModCommandSupport.ANYONE, null);
    }
}

package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.core.GuLifeState;
import net.minecraft.commands.CommandSourceStack;

//  /gzr lifestate set <v>
//  Ungated, top level not under core: a body needs no aperture to be zombified -- see GuLifeState.
public final class CmdLifeState {

    private CmdLifeState() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return ModCommandSupport.enumSetNode("lifestate", GuLifeState.values(), CoreService::setLifeState,
                ModCommandSupport.ANYONE, null);
    }
}

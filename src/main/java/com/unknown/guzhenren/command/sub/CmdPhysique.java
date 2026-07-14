package com.unknown.guzhenren.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.unknown.guzhenren.attachment.service.CoreService;
import com.unknown.guzhenren.command.ModCommandSupport;
import com.unknown.guzhenren.custom.enums.core.GuExtremePhysique;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//  /gzr physique extreme set <v>
//
//  `physique extreme set`, not `physique set`: 十绝体质 is one *kind* of physique and there will be
//  others, so the kind gets a node rather than being crammed into one flat enum.
public final class CmdPhysique {

    private CmdPhysique() {}

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("physique")
                .requires(ModCommandSupport::sourceAwakened)
                .then(ModCommandSupport.enumSetNode("extreme", GuExtremePhysique.values(),
                        CoreService::setExtremePhysique,
                        ModCommandSupport.AWAKENED, ModCommandSupport.FAILED_UNAWAKENED));
    }
}

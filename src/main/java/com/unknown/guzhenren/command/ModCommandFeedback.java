package com.unknown.guzhenren.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

//  The three feedback classes -- default / green success / red failure, all tagged [GZR].
//  See CLAUDE.md "Color means exactly one thing".
//
//  Every message routes through here, so the tag is never a decision. Vanilla is close but not close
//  enough: sendFailure paints red but drops the tag, sendSuccess paints nothing.
public final class ModCommandFeedback {

    private ModCommandFeedback() {}

    //  Printed once at the top of an info block; the detail lines under it need no tag of their own.
    public static void header(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("guzhenren.command.header"), false);
    }

    public static void detail(CommandSourceStack source, Component line) {
        source.sendSuccess(() -> line, false);
    }

    public static void success(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.GREEN), false);
    }

    //  Not sendFailure: that paints red but drops the tag, and it also flags the command as failed to
    //  anything listening. A partial refusal (three targets updated, one refused) is not a failure of
    //  the command -- it is a result, and it has to be able to sit next to a green line.
    public static void failure(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.RED), false);
    }

    private static Component tagged(Component message, ChatFormatting color) {
        return Component.translatable("guzhenren.command.tagged", message).withStyle(color);
    }
}

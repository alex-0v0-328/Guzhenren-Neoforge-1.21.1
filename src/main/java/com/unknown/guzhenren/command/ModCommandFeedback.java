package com.unknown.guzhenren.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

//  The three feedback classes -- default / green success / red failure. The [GZR] tag always stays
//  default-colored; only the message carries the class color. See CLAUDE.md "Color".
public final class ModCommandFeedback {

    private ModCommandFeedback() {}

    //  Once at the top of an info block; the detail lines under it need no tag of their own.
    public static void header(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("guzhenren.command.header"), false);
    }

    public static void detail(CommandSourceStack source, Component line) {
        source.sendSuccess(() -> line, false);
    }

    public static void success(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.GREEN), false);
    }

    //  Not sendFailure: it drops the tag, and it flags the whole command as failed. A partial refusal
    //  is a result, not a failure -- it has to be able to sit next to a green line.
    public static void failure(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.RED), false);
    }

    //  Color the message argument, not the whole line -- the "[GZR] " literal in the key stays default.
    private static Component tagged(Component message, ChatFormatting color) {
        return Component.translatable("guzhenren.command.tagged", message.copy().withStyle(color));
    }
}

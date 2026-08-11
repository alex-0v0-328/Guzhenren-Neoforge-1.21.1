package com.unknown.guzhenren.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Every reply a command sends, in one voice: plain for data, green for what changed, red for what did not.
 *
 * <p>⚠ Color carries exactly one meaning here, and that meaning is the category of the reply. It
 * never encodes a domain, a rank, or a severity.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class ModCommandFeedback {

    private ModCommandFeedback() {}

    public static void header(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("guzhenren.command.header"), false);
    }

    public static void detail(CommandSourceStack source, Component line) {
        source.sendSuccess(() -> line, false);
    }

    public static void success(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.GREEN), false);
    }

    public static void failure(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> tagged(message, ChatFormatting.RED), false);
    }

    private static Component tagged(Component message, ChatFormatting color) {
        return Component.translatable("guzhenren.command.tagged", message.copy().withStyle(color));
    }
}

package com.unknown.guzhenren.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

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

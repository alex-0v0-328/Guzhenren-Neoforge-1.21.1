package com.unknown.guzhenren.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

//  Enum arguments as a plain word plus suggestions.
//  Not NeoForge's EnumArgument -- that is a registered argument type; a word needs no registration.
public final class ModEnumArgument {

    private ModEnumArgument() {}

    private static final DynamicCommandExceptionType UNKNOWN_VALUE = new DynamicCommandExceptionType(
            value -> Component.translatable("guzhenren.command.unknown_value", value));

    public static <E extends Enum<E> & StringRepresentable> RequiredArgumentBuilder<CommandSourceStack, String> arg(
            String name, E[] values) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(values).map(StringRepresentable::getSerializedName), builder));
    }

    public static <E extends Enum<E> & StringRepresentable> E get(
            CommandContext<CommandSourceStack> context, String name, E[] values) throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, name);
        for (E value : values) {
            if (value.getSerializedName().equals(raw)) return value;
        }
        throw UNKNOWN_VALUE.create(raw);
    }
}

package com.unknown.guzhenren.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.CommandDispatcher;
import java.lang.reflect.Method;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

class ModCommandTest {

    @Test
    void soulWritesHaveTheirOwnRootDomain() throws Exception {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        Method register = ModCommand.class.getDeclaredMethod("register", CommandDispatcher.class);
        register.setAccessible(true);
        register.invoke(null, dispatcher);

        var root = dispatcher.getRoot().getChild("guzhenren");
        var soul = root.getChild("soul");
        assertNotNull(soul);
        assertNotNull(soul.getChild("max"));
        assertNotNull(soul.getChild("current"));
        assertNotNull(soul.getChild("refill"));
        assertNull(root.getChild("body").getChild("soul"));
    }
}

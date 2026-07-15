package com.unknown.guzhenren.client;

import com.unknown.guzhenren.Guzhenren;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

//  Client keybinds. Registered in ClientEvents; rebindable under Options -> Controls.
public final class ModKeyMappings {

    private ModKeyMappings() {}

    public static final String CATEGORY = "key.categories.guzhenren";

    //  Default G: next to E, unbound in vanilla. The player rebinds it in Controls.
    public static final KeyMapping OPEN_INFO =
            new KeyMapping("key.guzhenren.open_info", GLFW.GLFW_KEY_G, CATEGORY);
}

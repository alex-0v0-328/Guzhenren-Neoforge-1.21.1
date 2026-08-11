package com.unknown.guzhenren.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * The mod's key bindings.
 *
 * @author Alex
 * @since 1.0.0
 */
public final class ModKeyMappings {

    private ModKeyMappings() {}

    public static final String CATEGORY = "key.categories.guzhenren";

    public static final KeyMapping OPEN_INFO =
            new KeyMapping("key.guzhenren.open_info", GLFW.GLFW_KEY_G, CATEGORY);
}

package com.unknown.guzhenren.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * The mod's key bindings.
 *
 * <p>Currently a single binding: {@code OPEN_INFO} (G), which opens the
 * {@link com.unknown.guzhenren.client.screen.PlayerInfoScreen}. All keys live under the
 * {@code key.categories.guzhenren} category.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 * @see com.unknown.guzhenren.client.screen.PlayerInfoScreen
 */
public final class ModKeyMappings {

    private ModKeyMappings() {}

    public static final String CATEGORY = "key.categories.guzhenren";

    public static final KeyMapping OPEN_INFO =
            new KeyMapping("key.guzhenren.open_info", GLFW.GLFW_KEY_G, CATEGORY);
}

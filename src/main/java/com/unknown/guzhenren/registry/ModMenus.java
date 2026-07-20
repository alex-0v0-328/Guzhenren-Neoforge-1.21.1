package com.unknown.guzhenren.registry;

import com.unknown.guzhenren.Guzhenren;
import com.unknown.guzhenren.menu.ApertureStorageMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

//  Container menus. One today: an aperture's Gu store.
public final class ModMenus {

    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Guzhenren.MOD_ID);

    //  ⚠ Opened only from OpenApertureStoragePayload, which validates the aperture index first.
    //  The client rebuilds with aperture 0 / page 0; the server's real values arrive with the slots.
    //  ⚠ The `_menu` suffix is load-bearing: the attachment is already `aperture_storage`, and an id
    //  must find exactly one thing. Registry namespaces differ, but the SEARCH does not.
    public static final DeferredHolder<MenuType<?>, MenuType<ApertureStorageMenu>> APERTURE_STORAGE_MENU =
            MENUS.register("aperture_storage_menu", () -> new MenuType<>(
                    (id, inventory) -> new ApertureStorageMenu(id, inventory, 0, 0),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
